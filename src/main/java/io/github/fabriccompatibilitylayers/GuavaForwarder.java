package io.github.fabriccompatibilitylayers;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

import java.util.List;

public class GuavaForwarder {
    /**
     * Every Guava version this project has a sourceSet (and thus a
     * {@code GuavaVersionModuleImpl}) for, oldest first.
     */
    private static final List<String> SUPPORTED_VERSIONS = List.of(
            "12.0.1", "13.0", "13.0.1", "14.0", "14.0.1", "15.0", "16.0", "16.0.1", "17.0",
            "18.0", "19.0", "20.0", "21.0"
    );

    public static void registerAdditionalMappings(MappingBuilder builder, String fromVersion, String toVersion) {
        forEachModuleInRange(fromVersion, toVersion, module -> module.registerMappings(builder));
    }

    public static void registerVisitors(VisitorInfos visitorInfos, String fromVersion, String toVersion) {
        GuavaVersion from = GuavaVersion.parse(fromVersion);
        GuavaVersion to = GuavaVersion.parse(toVersion);
        forEachModuleInRange(fromVersion, toVersion, module -> module.registerVisitors(visitorInfos, from, to));
    }

    private static void forEachModuleInRange(String fromVersion, String toVersion, java.util.function.Consumer<GuavaVersionModule> action) {
        GuavaVersion from = GuavaVersion.parse(fromVersion);
        GuavaVersion to = GuavaVersion.parse(toVersion);

        if (from.equals(to)) {
            return;
        }

        for (String version : SUPPORTED_VERSIONS) {
            GuavaVersion parsed = GuavaVersion.parse(version);
            if (parsed.isNewerThan(from) && !parsed.isNewerThan(to)) {
                action.accept(loadModule(version));
            }
        }
    }

    private static GuavaVersionModule loadModule(String version) {
        String className = "io.github.fabriccompatibilitylayers.guavaforwarder." + sourceSetId(version) + ".GuavaVersionModuleImpl";
        try {
            return (GuavaVersionModule) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing Guava forwarder module for version " + version + " (expected " + className + ")", e);
        }
    }

    private static String sourceSetId(String version) {
        return "g" + version.replace('.', '_');
    }
}
