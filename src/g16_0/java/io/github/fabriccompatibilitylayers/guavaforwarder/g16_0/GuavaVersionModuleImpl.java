package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        builder.addMapping("com/google/common/hash/HashFunction")
                .method("hashString", "hashUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/HashCode;");
        builder.addMapping("com/google/common/hash/Hasher")
                .method("putString", "putUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/Hasher;");
        builder.addMapping("com/google/common/hash/PrimitiveSink")
                .method("putString", "putUnencodedChars", "(Ljava/lang/CharSequence;)Lcom/google/common/hash/PrimitiveSink;");
        builder.addMapping("com/google/common/hash/Funnels")
                .method("stringFunnel", "unencodedCharsFunnel", "()Lcom/google/common/hash/Funnel;");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
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
