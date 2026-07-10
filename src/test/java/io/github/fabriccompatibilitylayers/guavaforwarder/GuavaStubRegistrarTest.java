package io.github.fabriccompatibilitylayers.guavaforwarder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuavaStubRegistrarTest {
    @GuavaStubs("com/example/Old")
    static class StubHolder {
        @GuavaStub
        public static void alwaysPresent() {
        }

        @GuavaStub(introducedIn = "99.0")
        public static void introducedLate() {
        }

        // Original was an instance method: the receiver is passed as this stub's
        // leading parameter and must be excluded from the inferred original descriptor.
        @GuavaStub(staticOriginal = false)
        public static int instanceMethod(StubHolder self, String arg) {
            return arg.length();
        }
    }

    @Test
    void skipsStubsIntroducedAfterFromVersion() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaStubRegistrar.register(visitorInfos, GuavaVersion.parse("17.0"), StubHolder.class);

        assertEquals(2, visitorInfos.methodRedirects.size());
        FakeVisitorInfos.MethodRedirect redirect = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetMethod().equals("alwaysPresent"))
                .findFirst().orElseThrow();
        assertEquals("com/example/Old", redirect.targetClass());
        assertEquals("()V", redirect.targetDesc());
        assertEquals(StubHolder.class.getName().replace('.', '/'), redirect.replacementOwner());
        assertEquals("alwaysPresent", redirect.replacementName());
        assertEquals("()V", redirect.replacementDesc());
        assertTrue(redirect.replacementStatic());
    }

    @Test
    void infersOriginalDescriptorExcludingLeadingReceiverParam() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaStubRegistrar.register(visitorInfos, GuavaVersion.parse("17.0"), StubHolder.class);

        FakeVisitorInfos.MethodRedirect redirect = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetMethod().equals("instanceMethod"))
                .findFirst().orElseThrow();
        // Original: int instanceMethod(String) - receiver dropped, stub's own params kept.
        assertEquals("(Ljava/lang/String;)I", redirect.targetDesc());
        // Replacement keeps the full stub signature, receiver included.
        assertEquals("(L" + StubHolder.class.getName().replace('.', '/') + ";Ljava/lang/String;)I", redirect.replacementDesc());
    }

    @GuavaAdapter("com/example/OldInterface")
    static class AdapterHolder {
    }

    @GuavaAdapter(value = "com/example/OldLateInterface", introducedIn = "99.0")
    static class LateAdapterHolder {
    }

    @Test
    void registersClassRenameForAdapter() {
        FakeMappingBuilder mappingBuilder = new FakeMappingBuilder();

        GuavaStubRegistrar.registerAdapters(mappingBuilder, GuavaVersion.parse("17.0"), AdapterHolder.class);

        assertEquals(1, mappingBuilder.classRenames.size());
        FakeMappingBuilder.ClassRename rename = mappingBuilder.classRenames.get(0);
        assertEquals("com/example/OldInterface", rename.from());
        assertEquals(AdapterHolder.class.getName().replace('.', '/'), rename.to());
    }

    @Test
    void skipsAdaptersIntroducedAfterFromVersion() {
        FakeMappingBuilder mappingBuilder = new FakeMappingBuilder();

        GuavaStubRegistrar.registerAdapters(mappingBuilder, GuavaVersion.parse("17.0"), LateAdapterHolder.class);

        assertEquals(0, mappingBuilder.classRenames.size());
    }
}
