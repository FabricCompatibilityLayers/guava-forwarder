package io.github.fabriccompatibilitylayers.guavaforwarder.g14_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        builder.addMapping("com/google/common/collect/AbstractLinkedIterator", "com/google/common/collect/AbstractSequentialIterator");
        // AsynchronousComputationException was just a body-less subclass of ComputationException
        // with the same (Throwable) constructor - removed outright rather than renamed, but a
        // straight class rename to its still-present superclass is behaviorally identical.
//        builder.addMapping("com/google/common/collect/AsynchronousComputationException", "com/google/common/collect/ComputationException");
        builder.addMapping("com/google/common/collect/Iterators")
                .method("skip", "advance", "(Ljava/util/Iterator;I)I");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        visitorInfos.registerMethodInvocation(
                "com/google/common/base/Equivalences",
                "equals",
                "()Lcom/google/common/base/Equivalence;",
                VisitorInfos.classMember(
                        "com/google/common/base/Equivalence",
                        "equals",
                        "()Lcom/google/common/base/Equivalence;",
                        true
                )
        );
        visitorInfos.registerMethodInvocation(
                "com/google/common/base/Equivalences",
                "identity",
                "()Lcom/google/common/base/Equivalence;",
                VisitorInfos.classMember(
                        "com/google/common/base/Equivalence",
                        "identity",
                        "()Lcom/google/common/base/Equivalence;",
                        true
                )
        );
    }
}
