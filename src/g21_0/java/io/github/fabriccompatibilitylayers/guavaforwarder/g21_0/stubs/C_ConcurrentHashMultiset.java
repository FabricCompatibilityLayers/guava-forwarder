package io.github.fabriccompatibilitylayers.guavaforwarder.g21_0.stubs;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.MapMaker;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/collect/ConcurrentHashMultiset")
public class C_ConcurrentHashMultiset {
    @GuavaStub
    public static <E> ConcurrentHashMultiset<E> create(MapMaker mapMaker) {
        return ConcurrentHashMultiset.create(mapMaker.makeMap());
    }
}
