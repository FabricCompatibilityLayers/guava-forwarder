package io.github.fabriccompatibilitylayers;

import io.github.fabriccompatibilitylayers.guavaforwarder.FakeMappingBuilder;
import io.github.fabriccompatibilitylayers.guavaforwarder.FakeVisitorInfos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuavaForwarderTest {
    @Test
    void registersMappingsFromEveryVersionInRange() {
        FakeMappingBuilder builder = new FakeMappingBuilder();

        GuavaForwarder.registerAdditionalMappings(builder, "12.0.1", "21.0");

        assertTrue(builder.classRenames.contains(
                new FakeMappingBuilder.ClassRename(
                        "com/google/common/collect/AbstractLinkedIterator",
                        "com/google/common/collect/AbstractSequentialIterator"
                )
        ));
        assertTrue(builder.methodRenames.contains(
                new FakeMappingBuilder.MethodRename(
                        "com/google/common/hash/HashFunction",
                        "hashString", "hashUnencodedChars",
                        "(Ljava/lang/CharSequence;)Lcom/google/common/hash/HashCode;"
                )
        ));
        assertTrue(builder.classRenames.contains(
                new FakeMappingBuilder.ClassRename(
                        "com/google/common/base/Objects$ToStringHelper",
                        "com/google/common/base/MoreObjects$ToStringHelper"
                )
        ));
    }

    @Test
    void registersVisitorsAndStubsFromEveryVersionInRange() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        assertTrue(visitorInfos.methodRedirects.stream().anyMatch(r ->
                r.targetClass().equals("com/google/common/base/Equivalences")
                        && r.targetMethod().equals("identity")
                        && r.replacementOwner().equals("com/google/common/base/Equivalence")
        ));
        assertTrue(visitorInfos.methodRedirects.stream().anyMatch(r ->
                r.targetClass().equals("com/google/common/hash/HashCodes")
                        && r.targetMethod().equals("fromBytes")
                        && r.replacementOwner().equals("com/google/common/hash/HashCode")
        ));
        assertTrue(visitorInfos.methodRedirects.stream().anyMatch(r ->
                r.targetClass().equals("com/google/common/io/Files")
                        && r.targetMethod().equals("getDigest")
                        && r.replacementName().equals("getDigest")
        ));
    }

    @Test
    void skipsVersionsAtOrBelowFromVersion() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        // Starting at 14.0 already means the mod postdates the 13.0 getDigest removal,
        // so the g13_0 stub redirect should not be registered.
        GuavaForwarder.registerVisitors(visitorInfos, "14.0", "17.0");

        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/Files")
        ));
    }

    @Test
    void skipsInputSupplierStubOncePastRemovalIn20() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        // ByteStreams#getDigest's stub replacement declares a leading InputSupplier
        // parameter (to match the original member's descriptor) - InputSupplier itself
        // was removed in 20.0, so bridging past that point can never use this redirect.
        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/ByteStreams")
        ));
    }

    @Test
    void registersToStringHelperRedirectForEveryFactoryOverload() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        List<FakeVisitorInfos.MethodRedirect> toStringHelperRedirects = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/base/Objects") && r.targetMethod().equals("toStringHelper"))
                .toList();

        Set<String> targetDescriptors = toStringHelperRedirects.stream()
                .map(FakeVisitorInfos.MethodRedirect::targetDesc)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "(Ljava/lang/Object;)Lcom/google/common/base/Objects$ToStringHelper;",
                "(Ljava/lang/Class;)Lcom/google/common/base/Objects$ToStringHelper;",
                "(Ljava/lang/String;)Lcom/google/common/base/Objects$ToStringHelper;"
        ), targetDescriptors);

        for (FakeVisitorInfos.MethodRedirect redirect : toStringHelperRedirects) {
            assertEquals("com/google/common/base/MoreObjects", redirect.replacementOwner());
            assertEquals("toStringHelper", redirect.replacementName());
            assertTrue(redirect.replacementStatic());
            // Replacement returns MoreObjects$ToStringHelper - the paired class rename
            // (see registersMappingsFromEveryVersionInRange) is what makes call sites
            // that store/chain off the result still resolve correctly.
            assertEquals(
                    redirect.targetDesc().replace("Objects$ToStringHelper", "MoreObjects$ToStringHelper"),
                    redirect.replacementDesc()
            );
        }
    }

    @Test
    void doesNotRegisterToStringHelperRedirectBelow21_0() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        // g21_0 is only invoked once bridging reaches 21.0 - at 20.0, Objects$ToStringHelper
        // still exists, so no redirect should be registered yet.
        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "20.0");

        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/base/Objects") && r.targetMethod().equals("toStringHelper")
        ));
    }

    @Test
    void doesNotRegisterToStringHelperClassRenameBelow21_0() {
        FakeMappingBuilder builder = new FakeMappingBuilder();

        GuavaForwarder.registerAdditionalMappings(builder, "12.0.1", "20.0");

        assertTrue(builder.classRenames.stream().noneMatch(r ->
                r.from().equals("com/google/common/base/Objects$ToStringHelper")
        ));
    }
}
