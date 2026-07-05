package io.github.fabriccompatibilitylayers.guavaforwarder.g13_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs.I_ByteStreams;
import io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs.I_Files;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion) {
        // Removed in Guava 13.0, no straight replacement.
        GuavaStubRegistrar.register(visitorInfos, fromVersion, I_Files.class);
        GuavaStubRegistrar.register(visitorInfos, fromVersion, I_ByteStreams.class);
    }
}
