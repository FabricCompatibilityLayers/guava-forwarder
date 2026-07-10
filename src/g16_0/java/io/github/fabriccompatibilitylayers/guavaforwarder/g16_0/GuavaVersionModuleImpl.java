package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs.*;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    private static final GuavaVersion INPUT_SUPPLIER_REMOVED_IN = GuavaVersion.parse("20.0");

    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.registerAdapters(builder, fromVersion, C_Constraint.class);

        builder.addMapping("com/google/common/hash/HashFunction")
                .method("hashString", "hashUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/HashCode;");
        builder.addMapping("com/google/common/hash/Hasher")
                .method("putString", "putUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/Hasher;");
        builder.addMapping("com/google/common/hash/PrimitiveSink")
                .method("putString", "putUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/PrimitiveSink;");
        builder.addMapping("com/google/common/hash/Funnels")
                .method("stringFunnel", "unencodedCharsFunnel", "()Lcom/google/common/hash/Funnel;");
        // Same rename as Hasher#putString above, but the call site can also resolve
        // directly against this concrete base class (not just the Hasher interface) -
        // putUnencodedChars already exists here too, unlike the Beta-only breaks
        // elsewhere in this version bump.
        builder.addMapping("com/google/common/hash/AbstractStreamingHashFunction$AbstractStreamingHasher")
                .method("putString", "putUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/Hasher;");

        builder.addMapping("com/google/common/collect/ComparisonChain")
                .method("compare", "compareFalseFirst", "(ZZ)Lcom/google/common/collect/ComparisonChain;");

        builder.addMapping("com/google/common/net/InternetDomainName")
                .method("fromLenient", "from", "(Ljava/lang/String;)Lcom/google/common/net/InternetDomainName;")
                .method("name", "toString", "()Ljava/lang/String;");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.register(visitorInfos, fromVersion,
                C_Constraints.class,
                B_Stopwatch.class,
                C_Range.class,
                I_ByteStreams.class,
                I_CharStreams.class,
                I_Closeables.class
        );

        // I_BaseEncoding and I_FileBackedOutputStream's replacements take a leading
        // InputSupplier/OutputSupplier parameter (to match the original members'
        // descriptors) - both types were removed in Guava 20.0, so once the actual
        // runtime Guava is that new, no mod bytecode could hold an instance of either to
        // call these with anyway, and reflecting over the stub classes would fail trying
        // to resolve the removed types.
        if (toVersion.isOlderThan(INPUT_SUPPLIER_REMOVED_IN)) {
            GuavaStubRegistrar.register(visitorInfos, fromVersion,
                    I_BaseEncoding.class,
                    I_FileBackedOutputStream.class
            );
        }

        visitorInfos.registerMethodInvocation(
                "com/google/common/hash/HashCodes",
                "fromBytes",
                "([B)Lcom/google/common/hash/HashCode;",
                VisitorInfos.classMember(
                        "com/google/common/hash/HashCode",
                        "fromBytes",
                        "([B)Lcom/google/common/hash/HashCode;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/hash/HashCodes",
                "fromInt",
                "(I)Lcom/google/common/hash/HashCode;",
                VisitorInfos.classMember(
                        "com/google/common/hash/HashCode",
                        "fromInt",
                        "(I)Lcom/google/common/hash/HashCode;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/hash/HashCodes",
                "fromLong",
                "(J)Lcom/google/common/hash/HashCode;",
                VisitorInfos.classMember(
                        "com/google/common/hash/HashCode",
                        "fromLong",
                        "(J)Lcom/google/common/hash/HashCode;",
                        true
                )
        );
    }
}
