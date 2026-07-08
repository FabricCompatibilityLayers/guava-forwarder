package io.github.fabriccompatibilitylayers.guavaforwarder.g14_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerMappings(MappingBuilder builder) {
        builder.addMapping("com/google/common/collect/AbstractLinkedIterator", "com/google/common/collect/AbstractSequentialIterator");
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
