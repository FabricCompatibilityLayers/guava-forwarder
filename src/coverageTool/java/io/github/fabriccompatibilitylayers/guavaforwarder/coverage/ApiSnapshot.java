package io.github.fabriccompatibilitylayers.guavaforwarder.coverage;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The public/protected method surface of every public class in a single Guava jar,
 * as seen by bytecode compiled against it - i.e. what a mod jar could actually have
 * a call site referencing. Constructors/static initializers are excluded since
 * neither {@code GuavaStub} nor {@code VisitorInfos#registerMethodInvocation} can
 * redirect those.
 */
final class ApiSnapshot {
    /** {@code isPublic} is the original member's own visibility - {@code true} for {@code public},
     * {@code false} for {@code protected} - since a member demoted from public to protected needs
     * a subclass/same-package relationship to still be callable, exactly like a whole class demoted
     * from public to package-private (see {@link #resolvesMember}). */
    record Member(String owner, String name, String descriptor, boolean isPublic) {
    }

    /** A class's resolvable (public/protected, non-constructor) members - value is each member's
     * own {@code isPublic} flag, see {@link Member} - plus its hierarchy links. */
    private record ClassInfo(String superName, List<String> interfaces, Map<String, Boolean> resolvableMembers, boolean isPublic) {
    }

    /** The subset of each public class's {@code resolvableMembers} that excludes compiler-generated
     * bridge/synthetic methods - i.e. the hand-written surface a version bump is checked against. */
    private final Map<String, Map<String, Boolean>> methodsByClass = new HashMap<>();

    /** Every class in the jar (public or not), so hierarchy walks can pass through package-private
     * link classes too - keyed the same as {@link #methodsByClass}. */
    private final Map<String, ClassInfo> classInfo = new HashMap<>();

    private ApiSnapshot() {
    }

    static ApiSnapshot read(Path jar) throws IOException {
        ApiSnapshot snapshot = new ApiSnapshot();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                try (InputStream in = jarFile.getInputStream(entry)) {
                    new ClassReader(in).accept(
                            snapshot.new ClassCollector(),
                            ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE
                    );
                }
            }
        }
        return snapshot;
    }

    boolean hasClass(String internalName) {
        return methodsByClass.containsKey(internalName);
    }

    /** Classes present here that no longer exist at all in {@code other}. */
    List<String> classesMissingFrom(ApiSnapshot other) {
        List<String> missing = new ArrayList<>();
        for (String owner : methodsByClass.keySet()) {
            if (!other.hasClass(owner)) missing.add(owner);
        }
        Collections.sort(missing);
        return missing;
    }

    boolean hasAnyMembers(String owner) {
        Map<String, Boolean> members = methodsByClass.get(owner);
        return members != null && !members.isEmpty();
    }

    /**
     * Members whose old call site - {@code owner}, {@code name}, {@code descriptor} - no longer
     * resolves at all, checked the same way the JVM resolves {@code invokevirtual} (JVMS 5.4.3.3,
     * simplified): first {@code owner} itself, then its superclass chain, then its superinterfaces
     * (transitively, interleaved with the superclass walk rather than strictly after it - close
     * enough for Guava's non-conflicting hierarchies). Bridge/synthetic methods count here even
     * though they're excluded from {@link #methodsByClass}, since the JVM doesn't care about those
     * flags when resolving a call site - a covariant-return override's auto-generated bridge is a
     * perfectly valid target for an old call site compiled against the pre-covariant descriptor.
     *
     * <p>A class outside this jar - most commonly a JDK supertype, since Guava classes routinely
     * extend/implement {@code java.util}/{@code java.lang} types (e.g. {@code ForwardingObject}
     * extends {@code Object}, many collection wrappers implement {@code java.util.Collection}) -
     * falls back to the real JDK class via reflection ({@link #resolvesMemberOnJdkClass}) rather
     * than the walk simply stopping dead at the jar boundary, since a member inherited from a JDK
     * supertype resolves at a real call site just as validly as one declared in the jar itself.
     *
     * <p>{@code owner} itself must still be public: an old call site's constant pool entry names
     * {@code owner} directly, so per JVMS 5.4.4, resolving that symbolic reference at all requires
     * {@code owner} to be accessible, before the method-resolution walk up its hierarchy even
     * begins - a class demoted from public to package-private is exactly as breaking as one removed
     * outright, even though it (and its still-public-looking methods) remain in {@link #classInfo}
     * so hierarchy walks reached via a *still-public* subtype can keep passing through it. Once
     * past this check, intermediate supertypes/interfaces found while walking don't need to be
     * public themselves - only the resolved member's own accessibility matters at that point.
     *
     * <p>{@code requirePublic} applies that same JVMS 5.4.4 access check to whichever member the
     * walk resolves to - {@code true} if the old call site's own member was {@code public} (see
     * {@link Member#isPublic}), meaning the replacement must be {@code public} too, since an
     * unrelated external caller can't invoke a member demoted to {@code protected} even though
     * method *resolution* (5.4.3.3, which doesn't care about access flags) still finds it. Once a
     * same-name-and-descriptor member is found anywhere in the walk, that resolution is final - the
     * JVM doesn't keep searching further up for a more-accessible one if the one it found fails the
     * access check, so this stops right there too instead of continuing past it.
     */
    boolean resolvesMember(String owner, String name, String descriptor, boolean requirePublic) {
        ClassInfo ownerInfo = classInfo.get(owner);
        if (ownerInfo != null && !ownerInfo.isPublic()) return false;
        return resolvesMember(owner, name, descriptor, requirePublic, new HashSet<>());
    }

    private boolean resolvesMember(String owner, String name, String descriptor, boolean requirePublic, Set<String> visited) {
        if (owner == null || !visited.add(owner)) return false;
        ClassInfo info = classInfo.get(owner);
        if (info == null) return resolvesMemberOnJdkClass(owner, name, descriptor, requirePublic, visited);
        Boolean foundIsPublic = info.resolvableMembers().get(key(name, descriptor));
        if (foundIsPublic != null) return !requirePublic || foundIsPublic;
        if (resolvesMember(info.superName(), name, descriptor, requirePublic, visited)) return true;
        for (String iface : info.interfaces()) {
            if (resolvesMember(iface, name, descriptor, requirePublic, visited)) return true;
        }
        return false;
    }

    /**
     * Same resolution as {@link #resolvesMember}, but for a class not present in this jar,
     * loaded from whatever JDK the coverage tool itself runs on. {@code owner} has already been
     * marked visited by the caller, so this only needs to walk further up from it.
     */
    private boolean resolvesMemberOnJdkClass(String owner, String name, String descriptor, boolean requirePublic, Set<String> visited) {
        Class<?> jdkClass = loadJdkClass(owner);
        if (jdkClass == null) return false;
        for (Method method : jdkClass.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (!(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))) continue;
            if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(descriptor)) {
                return !requirePublic || Modifier.isPublic(modifiers);
            }
        }
        Class<?> superclass = jdkClass.getSuperclass();
        if (superclass != null && resolvesMember(internalName(superclass), name, descriptor, requirePublic, visited)) return true;
        for (Class<?> iface : jdkClass.getInterfaces()) {
            if (resolvesMember(internalName(iface), name, descriptor, requirePublic, visited)) return true;
        }
        return false;
    }

    private static Class<?> loadJdkClass(String internalName) {
        try {
            return Class.forName(internalName.replace('/', '.'), false, ApiSnapshot.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    private static String internalName(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Members that no longer resolve from an old call site - see {@link #resolvesMember} - including
     * members of classes that vanished entirely, since a per-method visitor redirect can still cover
     * those individually (e.g. a whole class removed but each of its methods redirected to a new home).
     */
    List<Member> membersMissingFrom(ApiSnapshot other) {
        List<Member> missing = new ArrayList<>();
        for (Map.Entry<String, Map<String, Boolean>> entry : methodsByClass.entrySet()) {
            String owner = entry.getKey();
            for (Map.Entry<String, Boolean> member : entry.getValue().entrySet()) {
                String key = member.getKey();
                boolean isPublic = member.getValue();
                int split = key.indexOf(' ');
                String name = key.substring(0, split);
                String descriptor = key.substring(split + 1);
                if (!other.resolvesMember(owner, name, descriptor, isPublic)) {
                    missing.add(new Member(owner, name, descriptor, isPublic));
                }
            }
        }
        missing.sort(Comparator.comparing(Member::owner).thenComparing(Member::name));
        return missing;
    }

    private static String key(String name, String descriptor) {
        return name + " " + descriptor;
    }

    private class ClassCollector extends ClassVisitor {
        private String owner;
        private boolean publicClass;

        ClassCollector() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            owner = name;
            publicClass = (access & Opcodes.ACC_PUBLIC) != 0;
            if (publicClass) methodsByClass.computeIfAbsent(owner, k -> new HashMap<>());
            classInfo.put(owner, new ClassInfo(
                    superName,
                    interfaces == null ? List.of() : List.of(interfaces),
                    new HashMap<>(),
                    publicClass
            ));
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            boolean isPublicMember = (access & Opcodes.ACC_PUBLIC) != 0;
            boolean visibleMember = isPublicMember || (access & Opcodes.ACC_PROTECTED) != 0;
            boolean compilerGenerated = (access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0;
            boolean constructorLike = name.equals("<init>") || name.equals("<clinit>");
            if (constructorLike) return null;
            if (visibleMember) {
                classInfo.get(owner).resolvableMembers().put(key(name, descriptor), isPublicMember);
            }
            if (publicClass && visibleMember && !compilerGenerated) {
                methodsByClass.get(owner).put(key(name, descriptor), isPublicMember);
            }
            return null;
        }
    }
}
