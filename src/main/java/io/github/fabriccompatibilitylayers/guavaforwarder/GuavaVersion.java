package io.github.fabriccompatibilitylayers.guavaforwarder;

import java.util.Arrays;

public final class GuavaVersion implements Comparable<GuavaVersion> {
    private final int[] parts;

    private GuavaVersion(int[] parts) {
        this.parts = parts;
    }

    public static GuavaVersion parse(String version) {
        String[] split = version.split("\\.");
        int[] parts = new int[3];
        for (int i = 0; i < parts.length && i < split.length; i++) {
            parts[i] = Integer.parseInt(split[i]);
        }
        return new GuavaVersion(parts);
    }

    public boolean isNewerThan(GuavaVersion other) {
        return compareTo(other) > 0;
    }

    public boolean isAtLeast(GuavaVersion other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(GuavaVersion other) {
        for (int i = 0; i < parts.length; i++) {
            int cmp = Integer.compare(parts[i], other.parts[i]);
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GuavaVersion && Arrays.equals(parts, ((GuavaVersion) obj).parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    @Override
    public String toString() {
        return parts[0] + "." + parts[1] + "." + parts[2];
    }
}
