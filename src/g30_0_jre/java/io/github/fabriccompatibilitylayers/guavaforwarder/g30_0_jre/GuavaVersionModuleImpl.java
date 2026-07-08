package io.github.fabriccompatibilitylayers.guavaforwarder.g30_0_jre;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g30_0_jre.stubs.U_C_ServiceManager;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.register(visitorInfos, fromVersion, U_C_ServiceManager.class);
    }
}
