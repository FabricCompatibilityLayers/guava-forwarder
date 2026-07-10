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
        // so the g13_0 stub redirect should not be registered. toVersion is kept at/above
        // 21.0 (rather than e.g. 17.0) so the g14_0/g15_0 GenericMapMaker-dependent stubs
        // are skipped too - GenericMapMaker isn't on the test runtime's real Guava jar
        // (32.0.1-jre) at all, so reflecting over those stubs would throw regardless of
        // this test's real focus.
        GuavaForwarder.registerVisitors(visitorInfos, "14.0", "21.0");

        // Filtered to getDigest specifically (rather than the whole Files class) since
        // g15_0 also legitimately registers a same-class, different-method Files#getChecksum
        // redirect for this same range.
        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/Files") && r.targetMethod().equals("getDigest")
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

    @Test
    void skipsGenericMapMakerStubsOncePastRemovalIn20() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        // C_GenericMapMaker's replacements (g14_0's softKeys, g15_0's makeComputingMap)
        // both take a leading GenericMapMaker parameter (to match the original member's
        // descriptor) - GenericMapMaker itself was removed in Guava 20.0, so bridging past
        // that point can never use these redirects, and reflecting over the stub classes
        // would fail trying to resolve the removed type.
        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/collect/GenericMapMaker")
        ));
    }

    @Test
    void registersJoinerAndMapJoinerIntersectionTypeStubs() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        // Guava 12.0.1-14.0.1 had a single generic overload accepting a type both
        // Iterable and Iterator - erased to Iterable (the leftmost bound) - removed in
        // 15.0 in favor of the separate Iterable/Iterator overloads that still exist
        // today. g15_0's B_Joiner replaces it by forwarding to the (still-present)
        // Iterator overload.
        List<FakeVisitorInfos.MethodRedirect> joinerRedirects = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/base/Joiner"))
                .toList();
        assertEquals(3, joinerRedirects.size());
        assertTrue(joinerRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("appendTo")
                        && r.targetDesc().equals("(Ljava/lang/Appendable;Ljava/lang/Object;)Ljava/lang/Appendable;")
        ));
        assertTrue(joinerRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("appendTo")
                        && r.targetDesc().equals("(Ljava/lang/StringBuilder;Ljava/lang/Object;)Ljava/lang/StringBuilder;")
        ));
        assertTrue(joinerRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("join")
                        && r.targetDesc().equals("(Ljava/lang/Object;)Ljava/lang/String;")
        ));
        assertTrue(joinerRedirects.stream().allMatch(r ->
                r.replacementOwner().equals("io/github/fabriccompatibilitylayers/guavaforwarder/g15_0/stubs/B_Joiner")
        ));

        List<FakeVisitorInfos.MethodRedirect> mapJoinerRedirects = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/base/Joiner$MapJoiner"))
                .toList();
        assertEquals(3, mapJoinerRedirects.size());
        assertTrue(mapJoinerRedirects.stream().allMatch(r ->
                r.replacementOwner().equals("io/github/fabriccompatibilitylayers/guavaforwarder/g15_0/stubs/B_Joiner_MapJoiner")
        ));
    }

    @Test
    void registersStopwatchHashingAndFilesChecksumStubs() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        FakeVisitorInfos.MethodRedirect stopwatch = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/base/Stopwatch"))
                .findFirst().orElseThrow();
        assertEquals("toString", stopwatch.targetMethod());
        assertEquals("(I)Ljava/lang/String;", stopwatch.targetDesc());
        assertEquals("(Lcom/google/common/base/Stopwatch;I)Ljava/lang/String;", stopwatch.replacementDesc());

        FakeVisitorInfos.MethodRedirect hashing = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/hash/Hashing"))
                .findFirst().orElseThrow();
        assertEquals("padToLong", hashing.targetMethod());
        assertEquals("(Lcom/google/common/hash/HashCode;)J", hashing.targetDesc());
        assertTrue(hashing.replacementStatic());

        // Distinct from the pre-existing g13_0 Files#getDigest redirect (see
        // skipsVersionsAtOrBelowFromVersion).
        FakeVisitorInfos.MethodRedirect filesGetChecksum = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/io/Files") && r.targetMethod().equals("getChecksum"))
                .findFirst().orElseThrow();
        assertEquals("(Ljava/io/File;Ljava/util/zip/Checksum;)J", filesGetChecksum.targetDesc());
    }

    @Test
    void registersLimitInputStreamClassRename() {
        FakeMappingBuilder builder = new FakeMappingBuilder();

        GuavaForwarder.registerAdditionalMappings(builder, "12.0.1", "21.0");

        assertTrue(builder.classRenames.contains(
                new FakeMappingBuilder.ClassRename(
                        "com/google/common/io/LimitInputStream",
                        "io/github/fabriccompatibilitylayers/guavaforwarder/g15_0/stubs/I_LimitInputStream"
                )
        ));
    }

    @Test
    void registersConcurrentHashMultisetCreateStub() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        FakeVisitorInfos.MethodRedirect redirect = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/collect/ConcurrentHashMultiset"))
                .findFirst().orElseThrow();
        assertEquals("create", redirect.targetMethod());
        assertEquals(
                "(Lcom/google/common/collect/MapMaker;)Lcom/google/common/collect/ConcurrentHashMultiset;",
                redirect.targetDesc()
        );
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g21_0/stubs/C_ConcurrentHashMultiset", redirect.replacementOwner());
        assertTrue(redirect.replacementStatic());
    }

    @Test
    void registersValueGraphBuilderStubOnlyWhenFromVersionIsAtLeast20_0() {
        // G_ValueGraphBuilder#from is annotated introducedIn = "20.0": Graph didn't exist
        // before then, so a mod compiled against an older Guava physically cannot call it.
        // Regression coverage for a bug where g22_0 passed a hardcoded
        // GuavaVersion.parse("20.0") to GuavaStubRegistrar instead of the real fromVersion,
        // which would satisfy the introducedIn gate unconditionally regardless of the
        // actual bridging range.
        FakeVisitorInfos belowIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(belowIntroduction, "12.0.1", "22.0");
        assertTrue(belowIntroduction.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/graph/ValueGraphBuilder")
        ));

        FakeVisitorInfos atIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(atIntroduction, "20.0", "22.0");
        FakeVisitorInfos.MethodRedirect redirect = atIntroduction.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/graph/ValueGraphBuilder"))
                .findFirst().orElseThrow();
        assertEquals("from", redirect.targetMethod());
        assertEquals(
                "(Lcom/google/common/graph/Graph;)Lcom/google/common/graph/ValueGraphBuilder;",
                redirect.targetDesc()
        );
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g22_0/stubs/G_ValueGraphBuilder", redirect.replacementOwner());
    }

    @Test
    void registersServiceManagerAddListenerStubOnlyWhenFromVersionIsAtLeast15_0() {
        // Same regression coverage as registersValueGraphBuilderStubOnlyWhenFromVersionIsAtLeast20_0,
        // for g30_0_jre, which passed a hardcoded GuavaVersion.parse("15.0") instead of
        // fromVersion.
        FakeVisitorInfos belowIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(belowIntroduction, "12.0.1", "30.0-jre");
        assertTrue(belowIntroduction.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/util/concurrent/ServiceManager")
        ));

        FakeVisitorInfos atIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(atIntroduction, "15.0", "30.0-jre");
        FakeVisitorInfos.MethodRedirect redirect = atIntroduction.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/util/concurrent/ServiceManager"))
                .findFirst().orElseThrow();
        assertEquals("addListener", redirect.targetMethod());
        assertEquals("(Lcom/google/common/util/concurrent/ServiceManager$Listener;)V", redirect.targetDesc());
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g30_0_jre/stubs/U_C_ServiceManager", redirect.replacementOwner());
        assertTrue(redirect.replacementStatic());
    }

    @Test
    void registersConstraintClassRenameAndConstraintsStubs() {
        FakeMappingBuilder mappingBuilder = new FakeMappingBuilder();
        GuavaForwarder.registerAdditionalMappings(mappingBuilder, "12.0.1", "21.0");

        // g16_0's C_Constraint is a @GuavaAdapter (full drop-in replacement), not a
        // @GuavaStub - Constraint itself was demoted from public in later Guava, so
        // there's no remaining member on the original type to redirect individual call
        // sites to; every reference to the type has to be renamed instead.
        assertTrue(mappingBuilder.classRenames.contains(
                new FakeMappingBuilder.ClassRename(
                        "com/google/common/collect/Constraint",
                        "io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/C_Constraint"
                )
        ));

        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        List<FakeVisitorInfos.MethodRedirect> constraintsRedirects = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/collect/Constraints"))
                .toList();

        // Every C_Constraints redirect's descriptor refers to C_Constraint (not the
        // original Constraint) - the class rename above runs first, so by the time this
        // redirect's matcher sees a call site, any Constraint occurrence in its
        // descriptor has already been rewritten to C_Constraint.
        String constraintDesc = "Lio/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/C_Constraint;";
        assertTrue(constraintsRedirects.stream().allMatch(r ->
                r.replacementOwner().equals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/C_Constraints")
                        && r.targetDesc().contains(constraintDesc)
        ));

        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("notNull") && r.targetDesc().equals("()" + constraintDesc)
        ));
        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("constrainedCollection")
                        && r.targetDesc().equals("(Ljava/util/Collection;" + constraintDesc + ")Ljava/util/Collection;")
        ));
        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("constrainedSet")
                        && r.targetDesc().equals("(Ljava/util/Set;" + constraintDesc + ")Ljava/util/Set;")
        ));
        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("constrainedSortedSet")
                        && r.targetDesc().equals("(Ljava/util/SortedSet;" + constraintDesc + ")Ljava/util/SortedSet;")
        ));
        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("constrainedList")
                        && r.targetDesc().equals("(Ljava/util/List;" + constraintDesc + ")Ljava/util/List;")
        ));
        assertTrue(constraintsRedirects.stream().anyMatch(r ->
                r.targetMethod().equals("constrainedMultiset")
                        && r.targetDesc().equals("(Lcom/google/common/collect/Multiset;" + constraintDesc + ")Lcom/google/common/collect/Multiset;")
        ));
    }

    @Test
    void registersStopwatchElapsedTimeAndElapsedMillisStubs() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        List<FakeVisitorInfos.MethodRedirect> stopwatchRedirects = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/base/Stopwatch")
                        && r.replacementOwner().equals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/B_Stopwatch"))
                .toList();

        FakeVisitorInfos.MethodRedirect elapsedTime = stopwatchRedirects.stream()
                .filter(r -> r.targetMethod().equals("elapsedTime"))
                .findFirst().orElseThrow();
        assertEquals("(Ljava/util/concurrent/TimeUnit;)J", elapsedTime.targetDesc());
        assertEquals("(Lcom/google/common/base/Stopwatch;Ljava/util/concurrent/TimeUnit;)J", elapsedTime.replacementDesc());

        FakeVisitorInfos.MethodRedirect elapsedMillis = stopwatchRedirects.stream()
                .filter(r -> r.targetMethod().equals("elapsedMillis"))
                .findFirst().orElseThrow();
        assertEquals("()J", elapsedMillis.targetDesc());
        assertEquals("(Lcom/google/common/base/Stopwatch;)J", elapsedMillis.replacementDesc());
    }

    @Test
    void registersRangeAsSetStub() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        FakeVisitorInfos.MethodRedirect redirect = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/collect/Range") && r.targetMethod().equals("asSet"))
                .findFirst().orElseThrow();
        assertEquals("(Lcom/google/common/collect/DiscreteDomain;)Lcom/google/common/collect/ContiguousSet;", redirect.targetDesc());
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/C_Range", redirect.replacementOwner());
        assertEquals(
                "(Lcom/google/common/collect/Range;Lcom/google/common/collect/DiscreteDomain;)Lcom/google/common/collect/ContiguousSet;",
                redirect.replacementDesc()
        );
    }

    @Test
    void registersCloseablesCloseQuietlyStub() {
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        FakeVisitorInfos.MethodRedirect redirect = visitorInfos.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/io/Closeables") && r.targetMethod().equals("closeQuietly"))
                .findFirst().orElseThrow();
        assertEquals("(Ljava/io/Closeable;)V", redirect.targetDesc());
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/I_Closeables", redirect.replacementOwner());
        assertTrue(redirect.replacementStatic());
    }

    @Test
    void registersByteStreamsAndCharStreamsAsSourceStubsOnlyWhenFromVersionIsAtLeast14_0() {
        // Both I_ByteStreams#asByteSource and I_CharStreams#asCharSource are annotated
        // introducedIn = "14.0" - ByteSource/CharSource didn't exist before then, so a
        // mod compiled against an older Guava physically cannot call them.
        FakeVisitorInfos belowIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(belowIntroduction, "12.0.1", "21.0");
        assertTrue(belowIntroduction.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/ByteStreams") && r.targetMethod().equals("asByteSource")
        ));
        assertTrue(belowIntroduction.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/CharStreams") && r.targetMethod().equals("asCharSource")
        ));

        FakeVisitorInfos atIntroduction = new FakeVisitorInfos();
        GuavaForwarder.registerVisitors(atIntroduction, "14.0", "21.0");

        FakeVisitorInfos.MethodRedirect byteStreams = atIntroduction.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/io/ByteStreams") && r.targetMethod().equals("asByteSource"))
                .findFirst().orElseThrow();
        assertEquals("([B)Lcom/google/common/io/ByteSource;", byteStreams.targetDesc());
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/I_ByteStreams", byteStreams.replacementOwner());

        FakeVisitorInfos.MethodRedirect charStreams = atIntroduction.methodRedirects.stream()
                .filter(r -> r.targetClass().equals("com/google/common/io/CharStreams") && r.targetMethod().equals("asCharSource"))
                .findFirst().orElseThrow();
        assertEquals("(Ljava/lang/String;)Lcom/google/common/io/CharSource;", charStreams.targetDesc());
        assertEquals("io/github/fabriccompatibilitylayers/guavaforwarder/g16_0/stubs/I_CharStreams", charStreams.replacementOwner());
    }

    @Test
    void skipsBaseEncodingAndFileBackedOutputStreamStubsOncePastInputSupplierRemovalIn20() {
        // I_BaseEncoding#decodingStream/encodingStream and
        // I_FileBackedOutputStream#getSupplier all take a leading InputSupplier or
        // OutputSupplier parameter (to match the original members' descriptors) - both
        // types were removed in Guava 20.0, so once the actual runtime Guava is that
        // new, no mod bytecode could hold an instance of either to call these with
        // anyway, and reflecting over the stub classes would fail trying to resolve the
        // removed types. Regression coverage for g16_0 registering these unconditionally
        // instead of gating them the way g13_0/g15_0 gate their own InputSupplier-taking
        // stubs.
        FakeVisitorInfos visitorInfos = new FakeVisitorInfos();

        GuavaForwarder.registerVisitors(visitorInfos, "12.0.1", "21.0");

        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/BaseEncoding")
        ));
        assertTrue(visitorInfos.methodRedirects.stream().noneMatch(r ->
                r.targetClass().equals("com/google/common/io/FileBackedOutputStream")
        ));
    }
}
