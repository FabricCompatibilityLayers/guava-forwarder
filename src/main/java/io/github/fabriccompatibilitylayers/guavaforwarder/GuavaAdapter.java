package io.github.fabriccompatibilitylayers.guavaforwarder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a full drop-in replacement for the original Guava class/interface
 * named here - registered by {@link GuavaStubRegistrar#registerAdapters} as a straight
 * class rename, redirecting every reference to the original type onto this class, not
 * just calls to one of its members. Use this instead of {@link GuavaStubs}/{@link GuavaStub}
 * when the original type itself became unusable (removed outright, or demoted from
 * public), so there's no remaining member on the original type left to redirect
 * individual call sites to - e.g. an old call site that implements, extends, or simply
 * declares a variable/field/parameter of the original type needs the type itself
 * swapped out, which a per-method redirect can't do.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GuavaAdapter {
    /** Internal name (slashes) of the original class this class fully replaces. */
    String value();

    /**
     * Guava version the original type was first introduced in. Leave blank if it has
     * been present since the oldest supported version: since a mod can only reference a
     * type that existed in the Guava version it was compiled against, an adapter whose
     * target was introduced after {@code fromVersion} is unreachable and is skipped.
     */
    String introducedIn() default "";
}
