package io.github.fabriccompatibilitylayers.guavaforwarder.coverage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diffs each pair of consecutive supported Guava jars' public API and cross-checks
 * every removed/changed member against what {@code GuavaForwarder} actually registers
 * for that version range - mappings, hand-written visitor redirects, and
 * {@code GuavaStub}-generated redirects alike, since stubs funnel through the same
 * {@code registerMethodInvocation} call. Anything left over is a coverage gap.
 *
 * <p>{@code GuavaStubRegistrar} reflects over stub holder classes, which requires
 * whatever real Guava types they reference (e.g. {@code InputSupplier}, removed in
 * 20.0) to actually be loadable - true in real production, since a single environment's
 * actual Guava always matches the {@code toVersion} passed to {@code GuavaForwarder}.
 * To keep that same invariant true here despite testing many version pairs in one JVM,
 * each pair's {@code GuavaForwarder} call runs in its own {@link URLClassLoader} with
 * that pair's real "to" jar substituted in place of whatever fixed Guava jar is on this
 * process's own classpath.
 *
 * Args: {@code <outputFile> <version1> <jarPath1> <version2> <jarPath2> ...}, oldest
 * version first.
 */
public final class CoverageReportMain {
    private record ClassRenameInfo(String from, String to) {
    }

    private record MethodRenameInfo(String owner, String from, String descriptor) {
    }

    private record MethodRedirectInfo(String targetClass, String targetMethod, String targetDesc) {
    }

    private record PairRegistrations(List<ClassRenameInfo> classRenames, List<MethodRenameInfo> methodRenames,
                                      List<MethodRedirectInfo> methodRedirects) {
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        Path outputFile = Path.of(args[0]);

        LinkedHashMap<String, Path> jarsByVersion = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            jarsByVersion.put(args[i], Path.of(args[i + 1]));
        }
        List<String> versions = new ArrayList<>(jarsByVersion.keySet());

        Map<String, ApiSnapshot> snapshots = new LinkedHashMap<>();
        for (String version : versions) {
            snapshots.put(version, ApiSnapshot.read(jarsByVersion.get(version)));
        }

        List<URL> baseClasspath = baseClasspathExcludingGuava(jarsByVersion.values());

        StringBuilder report = new StringBuilder();
        report.append("Guava upgrade coverage report\n");
        report.append("=============================\n\n");

        int totalBreaks = 0;
        int totalUncovered = 0;

        for (int i = 1; i < versions.size(); i++) {
            String from = versions.get(i - 1);
            String to = versions.get(i);
            ApiSnapshot fromApi = snapshots.get(from);
            ApiSnapshot toApi = snapshots.get(to);

            PairRegistrations registrations = registerForPair(from, to, baseClasspath, jarsByVersion.get(to));

            Map<String, String> classRenameTarget = new HashMap<>();
            for (ClassRenameInfo rename : registrations.classRenames()) {
                classRenameTarget.put(rename.from(), rename.to());
            }

            List<String> removedClasses = fromApi.classesMissingFrom(toApi);
            List<ApiSnapshot.Member> removedMembers = fromApi.membersMissingFrom(toApi);

            List<ApiSnapshot.Member> uncoveredMembers = removedMembers.stream()
                    .filter(member -> !isCovered(member, registrations, toApi, classRenameTarget))
                    .toList();
            Set<String> uncoveredMemberOwners = uncoveredMembers.stream()
                    .map(ApiSnapshot.Member::owner)
                    .collect(Collectors.toSet());

            // A wholly-removed class only counts as covered if it was renamed, or if
            // every one of its members already has an individual redirect - a class
            // with no tracked public members (so nothing above could have covered it)
            // never counts as covered.
            List<String> uncoveredClasses = removedClasses.stream()
                    .filter(cls -> !classRenameTarget.containsKey(cls))
                    .filter(cls -> !fromApi.hasAnyMembers(cls) || uncoveredMemberOwners.contains(cls))
                    .toList();

            totalBreaks += removedClasses.size() + removedMembers.size();
            totalUncovered += uncoveredClasses.size() + uncoveredMembers.size();

            report.append(from).append(" -> ").append(to).append('\n');
            report.append(String.format("  classes removed: %d (uncovered: %d)%n", removedClasses.size(), uncoveredClasses.size()));
            report.append(String.format("  members removed: %d (uncovered: %d)%n", removedMembers.size(), uncoveredMembers.size()));
            for (String cls : uncoveredClasses) {
                report.append("    [class]  ").append(cls).append('\n');
            }
            for (ApiSnapshot.Member member : uncoveredMembers) {
                report.append("    [member] ").append(member.owner()).append('#')
                        .append(member.name()).append(member.descriptor()).append('\n');
            }
            report.append('\n');
        }

        report.append(String.format(
                "Total: %d breaking changes across supported versions, %d not yet covered by a mapping/visitor/stub.%n",
                totalBreaks, totalUncovered
        ));

        Files.writeString(outputFile, report.toString(), StandardCharsets.UTF_8);
        System.out.print(report);
    }

    /**
     * Every entry on this process's own classpath, minus any of the known per-version
     * Guava jars - so each pair's isolated loader can add back exactly the one real jar
     * matching its "to" version instead of inheriting whichever fixed version this
     * process happened to be launched with.
     */
    private static List<URL> baseClasspathExcludingGuava(Iterable<Path> allVersionJars) throws MalformedURLException {
        Set<Path> excluded = new HashSet<>();
        for (Path jar : allVersionJars) {
            excluded.add(jar.toAbsolutePath().normalize());
        }

        List<URL> urls = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path path = Path.of(entry).toAbsolutePath().normalize();
            if (excluded.contains(path)) continue;
            urls.add(path.toUri().toURL());
        }
        return urls;
    }

    private static PairRegistrations registerForPair(
            String from, String to, List<URL> baseClasspath, Path toJar
    ) throws ReflectiveOperationException, IOException {
        List<URL> urls = new ArrayList<>(baseClasspath);
        urls.add(toJar.toUri().toURL());

        try (URLClassLoader pairLoader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader())) {
            Class<?> mappingBuilderIface = Class.forName("io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder", true, pairLoader);
            Class<?> visitorInfosIface = Class.forName("io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos", true, pairLoader);
            Class<?> fakeMappingBuilderClass = Class.forName("io.github.fabriccompatibilitylayers.guavaforwarder.FakeMappingBuilder", true, pairLoader);
            Class<?> fakeVisitorInfosClass = Class.forName("io.github.fabriccompatibilitylayers.guavaforwarder.FakeVisitorInfos", true, pairLoader);
            Class<?> guavaForwarderClass = Class.forName("io.github.fabriccompatibilitylayers.GuavaForwarder", true, pairLoader);

            Object mappingBuilder = fakeMappingBuilderClass.getDeclaredConstructor().newInstance();
            Object visitorInfos = fakeVisitorInfosClass.getDeclaredConstructor().newInstance();

            try {
                guavaForwarderClass.getMethod("registerAdditionalMappings", mappingBuilderIface, String.class, String.class)
                        .invoke(null, mappingBuilder, from, to);
                guavaForwarderClass.getMethod("registerVisitors", visitorInfosIface, String.class, String.class)
                        .invoke(null, visitorInfos, from, to);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException runtimeException) throw runtimeException;
                if (e.getCause() instanceof Error error) throw error;
                throw new IllegalStateException(e.getCause());
            }

            List<ClassRenameInfo> classRenames = new ArrayList<>();
            for (Object rename : (List<?>) fakeMappingBuilderClass.getField("classRenames").get(mappingBuilder)) {
                classRenames.add(new ClassRenameInfo(invokeString(rename, "from"), invokeString(rename, "to")));
            }

            List<MethodRenameInfo> methodRenames = new ArrayList<>();
            for (Object rename : (List<?>) fakeMappingBuilderClass.getField("methodRenames").get(mappingBuilder)) {
                methodRenames.add(new MethodRenameInfo(invokeString(rename, "owner"), invokeString(rename, "from"), invokeString(rename, "descriptor")));
            }

            List<MethodRedirectInfo> methodRedirects = new ArrayList<>();
            for (Object redirect : (List<?>) fakeVisitorInfosClass.getField("methodRedirects").get(visitorInfos)) {
                methodRedirects.add(new MethodRedirectInfo(
                        invokeString(redirect, "targetClass"), invokeString(redirect, "targetMethod"), invokeString(redirect, "targetDesc")
                ));
            }

            return new PairRegistrations(classRenames, methodRenames, methodRedirects);
        }
    }

    private static String invokeString(Object recordInstance, String accessor) throws ReflectiveOperationException {
        return (String) recordInstance.getClass().getMethod(accessor).invoke(recordInstance);
    }

    /** Applies every known class rename to each "L&lt;owner&gt;;" type reference in a descriptor. */
    private static String rewriteDescriptor(String descriptor, Map<String, String> classRenameTarget) {
        String rewritten = descriptor;
        for (Map.Entry<String, String> rename : classRenameTarget.entrySet()) {
            rewritten = rewritten.replace("L" + rename.getKey() + ";", "L" + rename.getValue() + ";");
        }
        return rewritten;
    }

    private static boolean isCovered(
            ApiSnapshot.Member member,
            PairRegistrations registrations,
            ApiSnapshot toApi,
            Map<String, String> classRenameTarget
    ) {
        // A class rename - including a @GuavaAdapter's, since it's registered as a plain
        // MappingBuilder rename too - doesn't just cover call sites on the renamed class
        // itself; any OTHER member whose descriptor mentions that class (most commonly a
        // fluent-builder method returning "this", or here a stub method returning/taking
        // the adapter type in place of the original it replaces) gets that reference
        // rewritten too by the real remapper. So every comparison below needs the
        // rewritten descriptor - a redirect/rename registered straight off a stub or
        // hand-written visitor call naturally names the *replacement* type, since that's
        // the only type available to reference by the time that code is written.
        String rewrittenDescriptor = rewriteDescriptor(member.descriptor(), classRenameTarget);

        String renamedTo = classRenameTarget.get(member.owner());
        if (renamedTo != null && toApi.resolvesMember(renamedTo, member.name(), rewrittenDescriptor, member.isPublic())) {
            return true;
        }
        for (MethodRenameInfo rename : registrations.methodRenames()) {
            if (rename.owner().equals(member.owner())
                    && rename.from().equals(member.name())
                    && rename.descriptor().equals(rewrittenDescriptor)) {
                return true;
            }
        }
        for (MethodRedirectInfo redirect : registrations.methodRedirects()) {
            if (redirect.targetClass().equals(member.owner())
                    && redirect.targetMethod().equals(member.name())
                    && redirect.targetDesc().equals(rewrittenDescriptor)) {
                return true;
            }
        }
        return false;
    }
}
