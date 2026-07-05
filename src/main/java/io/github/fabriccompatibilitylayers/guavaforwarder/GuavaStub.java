package io.github.fabriccompatibilitylayers.guavaforwarder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a static method as the replacement implementation for the same-named member of
 * the class named by the enclosing type's {@link GuavaStubs} annotation, removed with
 * no straight rename available. The replacement's owner/name/descriptor are derived
 * reflectively by {@link GuavaStubRegistrar} from this method itself; the original
 * member's name is assumed to match this method's name, and its descriptor is also
 * inferred, from this method's parameters minus a leading receiver parameter when
 * {@link #staticOriginal} is false.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GuavaStub {
    /**
     * Whether the original member was static. If it wasn't, every call site pushed the
     * receiver onto the stack ahead of the arguments, so this static replacement must
     * declare it as an explicit leading parameter (the original member's owning type)
     * to match - that leading parameter is excluded when inferring the original
     * member's descriptor.
     */
    boolean staticOriginal() default true;

    /**
     * Guava version the original member was first introduced in. Leave blank if it has
     * been present since the oldest supported version: since a mod can only call
     * members that existed in the Guava version it was compiled against, a stub whose
     * target was introduced after {@code fromVersion} is unreachable and is skipped.
     */
    String introducedIn() default "";
}
