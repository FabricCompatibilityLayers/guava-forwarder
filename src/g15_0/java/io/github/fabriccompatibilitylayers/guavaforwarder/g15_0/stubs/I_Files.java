package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.io.Files;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.File;
import java.io.IOException;
import java.util.zip.Checksum;

@GuavaStubs("com/google/common/io/Files")
public class I_Files {
    @GuavaStub
    public static long getChecksum(File file, Checksum checksum)
            throws IOException
    {
        return I_ByteStreams.getChecksum(Files.newInputStreamSupplier(file), checksum);
    }
}
