package io.github.fabriccompatibilitylayers.guavaforwarder.coverage;

import io.github.fabriccompatibilitylayers.GuavaForwarder;
import io.github.fabriccompatibilitylayers.guavaforwarder.FakeMappingBuilder;
import io.github.fabriccompatibilitylayers.guavaforwarder.FakeVisitorInfos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diffs each pair of consecutive supported Guava jars' public API and cross-checks
 * every removed/changed member against what {@link GuavaForwarder} actually
 * registers for that version range - mappings, hand-written visitor redirects, and
 * {@code GuavaStub}-generated redirects alike, since stubs funnel through the same
 * {@code registerMethodInvocation} call. Anything left over is a coverage gap.
 *
 * Args: {@code <outputFile> <version1> <jarPath1> <version2> <jarPath2> ...}, oldest
 * version first.
 */
public final class CoverageReportMain {
    public static void main(String[] args) throws IOException {
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

            FakeMappingBuilder mappings = new FakeMappingBuilder();
            FakeVisitorInfos visitors = new FakeVisitorInfos();
            GuavaForwarder.registerAdditionalMappings(mappings, from, to);
            GuavaForwarder.registerVisitors(visitors, from, to);

            Map<String, String> classRenameTarget = new HashMap<>();
            for (FakeMappingBuilder.ClassRename rename : mappings.classRenames) {
                classRenameTarget.put(rename.from(), rename.to());
            }

            List<String> removedClasses = fromApi.classesMissingFrom(toApi);
            List<ApiSnapshot.Member> removedMembers = fromApi.membersMissingFrom(toApi);

            List<ApiSnapshot.Member> uncoveredMembers = removedMembers.stream()
                    .filter(member -> !isCovered(member, mappings, visitors, toApi, classRenameTarget))
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

    private static boolean isCovered(
            ApiSnapshot.Member member,
            FakeMappingBuilder mappings,
            FakeVisitorInfos visitors,
            ApiSnapshot toApi,
            Map<String, String> classRenameTarget
    ) {
        String renamedTo = classRenameTarget.get(member.owner());
        if (renamedTo != null && toApi.hasMember(renamedTo, member.name(), member.descriptor())) {
            return true;
        }
        for (FakeMappingBuilder.MethodRename rename : mappings.methodRenames) {
            if (rename.owner().equals(member.owner())
                    && rename.from().equals(member.name())
                    && rename.descriptor().equals(member.descriptor())) {
                return true;
            }
        }
        for (FakeVisitorInfos.MethodRedirect redirect : visitors.methodRedirects) {
            if (redirect.targetClass().equals(member.owner())
                    && redirect.targetMethod().equals(member.name())
                    && redirect.targetDesc().equals(member.descriptor())) {
                return true;
            }
        }
        return false;
    }
}
