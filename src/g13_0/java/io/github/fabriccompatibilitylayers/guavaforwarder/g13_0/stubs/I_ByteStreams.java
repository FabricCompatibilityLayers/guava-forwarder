package io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs;

import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

@GuavaStubs("com/google/common/io/ByteStreams")
public class I_ByteStreams {
    @GuavaStub
    public static byte[] getDigest(InputSupplier<? extends InputStream> supplier, MessageDigest messageDigest) throws IOException {
        return ByteStreams.hash(supplier, MessageDigestUtils.messageDigestToHashFunction(messageDigest)).asBytes();
    }
}
