package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.base.Joiner;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@GuavaStubs("com/google/common/base/Joiner$MapJoiner")
public class B_Joiner_MapJoiner {
    @GuavaStub(staticOriginal = false)
    public static <A extends Appendable, I extends Iterable<? extends Map.Entry<?, ?>> & Iterator<? extends Map.Entry<?, ?>>>
    A appendTo(Joiner.MapJoiner joiner, A appendable, Object entries) throws IOException {
        Iterator<? extends Map.Entry<?, ?>> iterator = (Iterator<? extends Map.Entry<?, ?>>) entries;
        return joiner.appendTo(appendable, iterator);
    }

    @GuavaStub(staticOriginal = false)
    public static <I extends Iterable<? extends Map.Entry<?, ?>> & Iterator<? extends Map.Entry<?, ?>>>
    StringBuilder appendTo(Joiner.MapJoiner joiner, StringBuilder builder, Object entries) {
        Iterator<? extends Map.Entry<?, ?>> iterator = (Iterator<? extends Map.Entry<?, ?>>) entries;
        return joiner.appendTo(builder, iterator);
    }

    @GuavaStub(staticOriginal = false)
    public static  <I extends Iterable<? extends Map.Entry<?, ?>> & Iterator<? extends Map.Entry<?, ?>>>
    String join(Joiner.MapJoiner joiner, Object entries) {
        Iterator<? extends Map.Entry<?, ?>> iterator = (Iterator<? extends Map.Entry<?, ?>>) entries;
        return joiner.join(iterator);
    }
}
