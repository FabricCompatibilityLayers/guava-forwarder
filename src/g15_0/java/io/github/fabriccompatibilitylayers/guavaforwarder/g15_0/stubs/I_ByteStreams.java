package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@GuavaStubs("com/google/common/io/ByteStreams")
public class I_ByteStreams {
    @GuavaStub
    public static long getChecksum(
            InputSupplier<? extends InputStream> supplier, final Checksum checksum)
            throws IOException {
        HashFunction function;

        if (checksum instanceof CRC32) {
            function = Hashing.crc32();
        } else if (checksum instanceof Adler32) {
            function = Hashing.adler32();
        } else {
            throw new IllegalArgumentException("Unsupported checksum type: " + checksum.getClass().getName());
        }

        return ByteStreams.asByteSource(supplier).hash(function).asLong();
    }
}
