package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.base.Stopwatch;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/base/Stopwatch")
public class B_Stopwatch {
    @GuavaStub(staticOriginal = false)
    public static String toString(Stopwatch stopwatch, int significantDigits) {
        if (significantDigits == 4) {
            return stopwatch.toString();
        }

        throw new UnsupportedOperationException("Significant digits must be 4 in newer versions.");
    }
}
