package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.io.Closeables;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@GuavaStubs("com/google/common/io/Closeables")
public class I_Closeables {
    static final Logger logger
            = Logger.getLogger(Closeables.class.getName());

    @GuavaStub
    public static void closeQuietly(Closeable closeable) {
        try {
            Closeables.close(closeable, true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException should not have been thrown.", e);
        }
    }
}
