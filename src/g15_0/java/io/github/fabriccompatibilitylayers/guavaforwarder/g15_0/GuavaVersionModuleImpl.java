package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs.*;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    private static final GuavaVersion GUAVA_13_0 = GuavaVersion.parse("13.0");
    private static final GuavaVersion GUAVA_14_0 = GuavaVersion.parse("14.0");
    private static final GuavaVersion INPUT_SUPPLIER_REMOVED_IN = GuavaVersion.parse("20.0");

    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        builder.addMapping("com/google/common/io/LimitInputStream", "io/github/fabriccompatibilitylayers/guavaforwarder/g15_0/stubs/I_LimitInputStream");

        MappingBuilder.ClassMapping fiMapping = builder.addMapping("com/google/common/collect/FluentIterable")
                .method("toImmutableList", "toList", "()Lcom/google/common/collect/ImmutableList;")
                .method("toImmutableSet", "toSet", "()Lcom/google/common/collect/ImmutableSet;")
                .method("toImmutableSortedSet", "toSortedSet", "(Ljava/util/Comparator;)Lcom/google/common/collect/ImmutableSortedSet;");

        if (fromVersion.isAtLeast(GUAVA_13_0)) {
            fiMapping.method("toSortedImmutableList", "toSortedList", "(Ljava/util/Comparator;)Lcom/google/common/collect/ImmutableList;");
        }

        builder.addMapping("com/google/common/hash/BloomFilter")
                .method("expectedFalsePositiveProbability", "expectedFpp", "()D");
        builder.addMapping("com/google/common/net/InternetDomainName")
                .method("isValidLenient", "isValid", "(Ljava/lang/String;)Z");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.register(visitorInfos, fromVersion,
                B_Joiner.class,
                B_Joiner_MapJoiner.class,
                B_Stopwatch.class,
                H_Hashing.class,
                I_Files.class
        );

        // I_ByteStreams#getChecksum takes a leading InputSupplier parameter (to match the
        // original member's descriptor) - InputSupplier itself was removed in Guava 20.0,
        // so once the actual runtime Guava is that new, no mod bytecode could hold an
        // InputSupplier instance to call this with anyway, and reflecting over the stub
        // class would fail trying to resolve that removed type.
        if (toVersion.isOlderThan(INPUT_SUPPLIER_REMOVED_IN)) {
            GuavaStubRegistrar.register(visitorInfos, fromVersion, I_ByteStreams.class);
        }

        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/DiscreteDomains",
                "integers",
                "()Lcom/google/common/collect/DiscreteDomain;",
                VisitorInfos.classMember(
                        "com/google/common/collect/DiscreteDomain",
                        "integers",
                        "()Lcom/google/common/collect/DiscreteDomain;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/DiscreteDomains",
                "longs",
                "()Lcom/google/common/collect/DiscreteDomain;",
                VisitorInfos.classMember(
                        "com/google/common/collect/DiscreteDomain",
                        "longs",
                        "()Lcom/google/common/collect/DiscreteDomain;",
                        true
                )
        );

        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "all",
                "()Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "all",
                        "()Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "atLeast",
                "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "atLeast",
                        "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "atMost",
                "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "atMost",
                        "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "closed",
                "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "closed",
                        "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "closedOpen",
                "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "closedOpen",
                        "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "downTo",
                "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "downTo",
                        "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "encloseAll",
                "(Ljava/lang/Iterable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "encloseAll",
                        "(Ljava/lang/Iterable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "greaterThan",
                "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "greaterThan",
                        "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "lessThan",
                "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "lessThan",
                        "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "open",
                "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "open",
                        "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "openClosed",
                "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "openClosed",
                        "(Ljava/lang/Comparable;Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "range",
                "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "range",
                        "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "singleton",
                "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "singleton",
                        "(Ljava/lang/Comparable;)Lcom/google/common/collect/Range;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Ranges",
                "upTo",
                "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Range",
                        "upTo",
                        "(Ljava/lang/Comparable;Lcom/google/common/collect/BoundType;)Lcom/google/common/collect/Range;",
                        true
                )
        );

        if (fromVersion.isAtLeast(GUAVA_14_0)) {
            visitorInfos.registerMethodInvocation(
                    "com/google/common/io/ByteSink",
                    "openBufferedStream",
                    "()Ljava/io/BufferedOutputStream;",
                    VisitorInfos.classMember(
                            "com/google/common/io/ByteSink",
                            "openBufferedStream",
                            "()Ljava/io/OutputStream;",
                            false
                    )
            );
            visitorInfos.registerMethodInvocation(
                    "com/google/common/io/ByteSource",
                    "openBufferedStream",
                    "()Ljava/io/BufferedInputStream;",
                    VisitorInfos.classMember(
                            "com/google/common/io/ByteSource",
                            "openBufferedStream",
                            "()Ljava/io/InputStream;",
                            false
                    )
            );
            visitorInfos.registerMethodInvocation(
                    "com/google/common/io/CharSink",
                    "openBufferedStream",
                    "()Ljava/io/BufferedWriter;",
                    VisitorInfos.classMember(
                            "com/google/common/io/CharSink",
                            "openBufferedStream",
                            "()Ljava/io/Writer;",
                            false
                    )
            );
        }
    }
}
