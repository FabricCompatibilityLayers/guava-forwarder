package io.github.fabriccompatibilitylayers.guavaforwarder.coverage;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
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
    record Member(String owner, String name, String descriptor) {
    }

    private final Map<String, Set<String>> methodsByClass = new HashMap<>();

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

    boolean hasMember(String owner, String name, String descriptor) {
        Set<String> members = methodsByClass.get(owner);
        return members != null && members.contains(key(name, descriptor));
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
        Set<String> members = methodsByClass.get(owner);
        return members != null && !members.isEmpty();
    }

    /**
     * Members that no longer resolve by the exact name+descriptor an old call site
     * would reference - including members of classes that vanished entirely, since a
     * per-method visitor redirect can still cover those individually (e.g. a whole
     * class removed but each of its methods redirected to a new home).
     */
    List<Member> membersMissingFrom(ApiSnapshot other) {
        List<Member> missing = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : methodsByClass.entrySet()) {
            String owner = entry.getKey();
            for (String key : entry.getValue()) {
                int split = key.indexOf(' ');
                String name = key.substring(0, split);
                String descriptor = key.substring(split + 1);
                if (!other.hasMember(owner, name, descriptor)) {
                    missing.add(new Member(owner, name, descriptor));
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
            if (publicClass) methodsByClass.computeIfAbsent(owner, k -> new HashSet<>());
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            boolean visibleMember = (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0;
            boolean compilerGenerated = (access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0;
            boolean constructorLike = name.equals("<init>") || name.equals("<clinit>");
            if (publicClass && visibleMember && !compilerGenerated && !constructorLike) {
                methodsByClass.get(owner).add(key(name, descriptor));
            }
            return null;
        }
    }
}
