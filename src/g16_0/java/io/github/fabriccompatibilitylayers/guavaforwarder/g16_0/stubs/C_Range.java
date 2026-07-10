package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/collect/Range")
public class C_Range {
    @GuavaStub(staticOriginal = false)
    public static <C extends Comparable> ContiguousSet<C> asSet(Range<C> instance, DiscreteDomain<C> domain) {
        return ContiguousSet.create(instance, domain);
    }
}
