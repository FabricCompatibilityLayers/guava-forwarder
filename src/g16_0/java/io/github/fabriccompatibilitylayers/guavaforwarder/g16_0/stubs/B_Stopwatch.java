package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.base.Stopwatch;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.util.concurrent.TimeUnit;

@GuavaStubs("com/google/common/base/Stopwatch")
public class B_Stopwatch {
    @GuavaStub(staticOriginal = false)
    public static long elapsedTime(Stopwatch instance, TimeUnit desiredUnit) {
        return instance.elapsed(desiredUnit);
    }

    @GuavaStub(staticOriginal = false)
    public static long elapsedMillis(Stopwatch instance) {
        return instance.elapsed(TimeUnit.MILLISECONDS);
    }
}
