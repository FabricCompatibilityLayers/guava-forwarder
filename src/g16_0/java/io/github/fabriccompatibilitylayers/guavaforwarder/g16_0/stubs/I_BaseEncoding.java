package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.io.BaseEncoding;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;

@GuavaStubs("com/google/common/io/BaseEncoding")
public class I_BaseEncoding {
    @GuavaStub(staticOriginal = false)
    public static InputSupplier<InputStream> decodingStream(
            BaseEncoding instance,
            final InputSupplier<? extends Reader> readerSupplier) {
        checkNotNull(readerSupplier);
        return () -> instance.decodingStream(readerSupplier.getInput());
    }

    @GuavaStub(staticOriginal = false)
    public static OutputSupplier<OutputStream> encodingStream(
            BaseEncoding instance,
            final OutputSupplier<? extends Writer> writerSupplier) {
        checkNotNull(writerSupplier);
        return () -> instance.encodingStream(writerSupplier.getOutput());
    }
}
