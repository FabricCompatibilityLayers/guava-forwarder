package io.github.fabriccompatibilitylayers.guavaforwarder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as holding {@link GuavaStub} replacement methods for a single
 * original Guava class, named here once instead of repeating it on every
 * {@link GuavaStub}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GuavaStubs {
    /** Internal name (slashes) of the original class these stubs replace members of. */
    String value();
}
