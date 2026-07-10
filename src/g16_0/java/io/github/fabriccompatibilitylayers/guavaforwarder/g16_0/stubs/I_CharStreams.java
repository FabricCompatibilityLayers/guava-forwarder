package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.io.CharSource;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/io/CharStreams")
public class I_CharStreams {
    @GuavaStub(introducedIn = "14.0")
    public static CharSource asCharSource(String string) {
        return CharSource.wrap(string);
    }
}
