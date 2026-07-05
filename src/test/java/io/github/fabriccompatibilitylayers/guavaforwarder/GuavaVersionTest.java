package io.github.fabriccompatibilitylayers.guavaforwarder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuavaVersionTest {
    @Test
    void parsesMissingComponentsAsZero() {
        assertEquals(GuavaVersion.parse("13.0.0"), GuavaVersion.parse("13.0"));
    }

    @Test
    void comparesByMajorMinorPatch() {
        assertTrue(GuavaVersion.parse("13.0").isNewerThan(GuavaVersion.parse("12.0.1")));
        assertTrue(GuavaVersion.parse("14.0.1").isNewerThan(GuavaVersion.parse("14.0")));
        assertFalse(GuavaVersion.parse("16.0").isNewerThan(GuavaVersion.parse("16.0.1")));
    }

    @Test
    void isAtLeastIncludesEqualVersions() {
        assertTrue(GuavaVersion.parse("17.0").isAtLeast(GuavaVersion.parse("17.0")));
        assertFalse(GuavaVersion.parse("16.0").isAtLeast(GuavaVersion.parse("17.0")));
    }
}
