package io.github.fabriccompatibilitylayers.guavaforwarder.g15_0.stubs;

import com.google.common.base.Function;
import com.google.common.collect.GenericMapMaker;
import com.google.common.collect.MapMaker;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.util.concurrent.ConcurrentMap;

@GuavaStubs("com/google/common/collect/MapMaker")
public class C_MapMaker {
    @GuavaStub(staticOriginal = false)
    public static <K, V> ConcurrentMap<K, V> makeComputingMap(MapMaker mapMaker, Function<? super K, ? extends V> computingFunction) {
        throw new UnsupportedOperationException("Use CacheBuilder");
    }
}
