package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.io.ByteSource;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/io/ByteStreams")
public class I_ByteStreams {
    @GuavaStub(introducedIn = "14.0")
    public static ByteSource asByteSource(byte[] b) {
        return ByteSource.wrap(b);
    }
}
