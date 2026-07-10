package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.io.FileBackedOutputStream;
import com.google.common.io.InputSupplier;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.InputStream;

@GuavaStubs("com/google/common/io/FileBackedOutputStream")
public class I_FileBackedOutputStream {
    @GuavaStub(staticOriginal = false)
    public static InputSupplier<InputStream> getSupplier(FileBackedOutputStream instance) {
        return instance.asByteSource();
    }
}
