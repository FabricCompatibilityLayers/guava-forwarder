package io.github.fabriccompatibilitylayers.guavaforwarder.g14_0.stubs;

import com.google.common.collect.GenericMapMaker;
import com.google.common.collect.MapMaker;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/collect/GenericMapMaker")
public class C_GenericMapMaker {
    @GuavaStub(staticOriginal = false)
    public static <K0, V0> GenericMapMaker<K0, V0> softKeys(GenericMapMaker<K0, V0> mapMaker) {
        throw new UnsupportedOperationException("Soft keys are not supported in this version");
    }
}
