package io.github.fabriccompatibilitylayers.guavaforwarder.g22_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g22_0.stubs.G_ValueGraphBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerMappings(MappingBuilder builder, GuavaVersion fromVersion, GuavaVersion toVersion) {
        builder.addMapping("com/google/common/net/HostAndPort")
                .method("getHostText", "getHost", "()Ljava/lang/String;");
    }

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.register(visitorInfos, fromVersion, G_ValueGraphBuilder.class);
    }
}
