package io.github.fabriccompatibilitylayers.guavaforwarder;

import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

/**
 * Implemented once per supported Guava version sourceSet (as
 * {@code GuavaVersionModuleImpl}), describing the fixes needed when a mod compiled
 * against an older Guava version is remapped past this version.
 */
public interface GuavaVersionModule {
    default void registerMappings(MappingBuilder builder) {
    }

    default void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion) {
    }
}
