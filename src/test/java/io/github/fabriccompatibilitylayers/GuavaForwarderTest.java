package io.github.fabriccompatibilitylayers;

import io.github.fabriccompatibilitylayers.guavaforwarder.FakeMappingBuilder;
import io.github.fabriccompatibilitylayers.guavaforwarder.FakeVisitorInfos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GuavaForwarderTest {
    @Test
    void registersMappingsFromEveryVersionInRange() {
        FakeMappingBuilder builder = new FakeMappingBuilder();

        GuavaForwarder.registerAdditionalMappings(builder, "12.0.1", "17.0");

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
    }

    @Test
    void registersVisitorsAndStubsFromEveryVersionInRange() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "17.0");

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
}
