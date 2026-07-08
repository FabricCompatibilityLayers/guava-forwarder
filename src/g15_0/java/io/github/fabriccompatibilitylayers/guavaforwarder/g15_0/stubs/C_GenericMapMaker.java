package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.base.Function;
import com.google.common.collect.GenericMapMaker;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.util.concurrent.ConcurrentMap;

@GuavaStubs("com/google/common/collect/GenericMapMaker")
public class C_GenericMapMaker {
    @GuavaStub(staticOriginal = false)
    public static <K0, V0, K extends K0, V extends V0> ConcurrentMap<K, V> makeComputingMap(GenericMapMaker<K0, V0> mapMaker, Function<? super K, ? extends V> computingFunction) {
        throw new UnsupportedOperationException("Use CacheBuilder");
    }
}
