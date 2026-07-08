package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.base.Joiner;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.IOException;
import java.util.Iterator;

@GuavaStubs("com/google/common/base/Joiner")
public class B_Joiner {
    @GuavaStub(staticOriginal = false)
    public static <A extends Appendable, I extends Iterable<?> & Iterator<?>>
    A appendTo(Joiner joiner, A appendable, Object parts) throws IOException {
        return joiner.appendTo(appendable, (Iterator<?>) parts);
    }

    @GuavaStub(staticOriginal = false)
    public static <I extends Iterable<?> & Iterator<?>>
    StringBuilder appendTo(Joiner joiner, StringBuilder builder, Object parts) {
        return joiner.appendTo(builder, (Iterator<?>) parts);
    }

    @GuavaStub(staticOriginal = false)
    public static <I extends Iterable<?> & Iterator<?>>
    String join(Joiner joiner, Object parts) {
        return joiner.join((Iterator<?>) parts);
    }
}
