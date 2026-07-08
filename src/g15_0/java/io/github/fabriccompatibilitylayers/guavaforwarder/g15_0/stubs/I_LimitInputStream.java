package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.io.ByteStreams;

import java.io.FilterInputStream;
import java.io.InputStream;

public class I_LimitInputStream extends FilterInputStream {
    public I_LimitInputStream(InputStream in, long limit) {
        super(ByteStreams.limit(in, limit));
    }
}
