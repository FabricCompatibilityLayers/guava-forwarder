package io.github.fabriccompatibilitylayers.guavaforwarder.g21_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        // Objects$ToStringHelper was removed outright in 21.0 - MoreObjects$ToStringHelper
        // (introduced in 18.0 as its replacement) has an identical add/addValue/
        // omitNullValues/toString method surface, so a straight class rename covers
        // every instance-method call site on it.
        builder.addMapping("com/google/common/base/Objects$ToStringHelper", "com/google/common/base/MoreObjects$ToStringHelper");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        // The class rename above doesn't cover these: Objects itself isn't renamed (its
        // other members, e.g. equal/hashCode, remain), so the toStringHelper factory
        // methods need their own redirect to their MoreObjects counterpart.
        for (String paramDescriptor : new String[]{
                "Ljava/lang/Object;",
                "Ljava/lang/Class;",
                "Ljava/lang/String;"
        }) {
            visitorInfos.registerMethodInvocation(
                    "com/google/common/base/Objects",
                    "toStringHelper",
                    "(" + paramDescriptor + ")Lcom/google/common/base/Objects$ToStringHelper;",
                    VisitorInfos.classMember(
                            "com/google/common/base/MoreObjects",
                            "toStringHelper",
                            "(" + paramDescriptor + ")Lcom/google/common/base/MoreObjects$ToStringHelper;",
                            true
                    )
            );
        }
    }
}
