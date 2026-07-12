package io.github.fabriccompatibilitylayers;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

import java.util.List;

public class GuavaForwarder {
    /**
     * Every Guava version this project supports, oldest first, grouped into consecutive
     * runs the {@code coverageReport} task confirms have zero removed classes/members
     * between them (i.e. no public API break) - such a run shares one sourceSet and thus
     * one {@code GuavaVersionModuleImpl}, looked up under the sourceSet id of the group's
     * first/oldest member. This mirrors the sourceSet grouping in build.gradle.kts - keep
     * both in sync, and re-run {@code coverageReport} before changing either when adding
     * a new version.
     */
    private static final List<List<String>> SUPPORTED_VERSION_GROUPS = List.of(
            List.of("12.0.1"),
            List.of("13.0", "13.0.1"),
            List.of("14.0", "14.0.1"),
            List.of("15.0"),
            List.of("16.0", "16.0.1"),
            List.of("17.0"),
            List.of("18.0"),
            List.of("19.0"),
            List.of("20.0"),
            List.of("21.0"),
            List.of("22.0"),
            List.of("23.0"),
            List.of("23.1-jre"),
            List.of("23.2-jre"),
            List.of("23.3-jre", "23.4-jre", "23.5-jre", "23.6-jre", "23.6.1-jre"),
            List.of("24.0-jre", "24.1-jre", "24.1.1-jre"),
            List.of("25.0-jre", "25.1-jre"),
            List.of("26.0-jre", "27.0-jre"),
            List.of("27.0.1-jre", "27.1-jre"),
            List.of("28.0-jre", "28.1-jre", "28.2-jre"),
            List.of("29.0-jre"),
            List.of("30.0-jre", "30.1-jre", "30.1.1-jre"),
            List.of(
                    "31.0-jre", "31.0.1-jre", "31.1-jre", "32.0.0-jre", "32.0.1-jre",
                    "32.1.0-jre", "32.1.1-jre", "32.1.2-jre", "32.1.3-jre",
                    "33.0.0-jre", "33.1.0-jre", "33.2.0-jre", "33.2.1-jre", "33.3.0-jre", "33.3.1-jre",
                    "33.4.0-jre", "33.4.1-jre", "33.4.2-jre", "33.4.3-jre", "33.4.4-jre",
                    "33.4.5-jre", "33.4.6-jre", "33.4.7-jre", "33.4.8-jre",
                    "33.5.0-jre", "33.6.0-jre"
            )
    );

    public static void registerAdditionalMappings(MappingBuilder builder, String fromVersion, String toVersion) {
        GuavaVersion from = GuavaVersion.parse(fromVersion);
        GuavaVersion to = GuavaVersion.parse(toVersion);
        forEachModuleInRange(fromVersion, toVersion, module -> module.registerMappings(builder, from, to));
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

        // Invoke each group's module at most once, even though the group may contain
        // several versions falling within (from, to] - they all share one class, so
        // invoking it per-member would just register the same fixes redundantly.
        for (List<String> group : SUPPORTED_VERSION_GROUPS) {
            boolean inRange = group.stream().map(GuavaVersion::parse)
                    .anyMatch(parsed -> parsed.isNewerThan(from) && !parsed.isNewerThan(to));
            if (inRange) {
                action.accept(loadModule(group.get(0)));
            }
        }
    }

    private static GuavaVersionModule loadModule(String representativeVersion) {
        String className = "io.github.fabriccompatibilitylayers.guavaforwarder." + mangle(representativeVersion) + ".GuavaVersionModuleImpl";
        try {
            return (GuavaVersionModule) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing Guava forwarder module for version " + representativeVersion + " (expected " + className + ")", e);
        }
    }

    private static String mangle(String version) {
        return "g" + version.replace('.', '_').replace('-', '_');
    }
}
