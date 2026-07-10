package io.github.fabriccompatibilitylayers.guavaforwarder;

import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class GuavaStubRegistrar {
    private GuavaStubRegistrar() {
    }

    /**
     * Registers a {@link VisitorInfos#registerMethodInvocation} redirect for every
     * {@link GuavaStub}-annotated method found in {@code stubHolders}, skipping stubs
     * whose target was introduced later than {@code fromVersion}.
     */
    public static void register(VisitorInfos visitorInfos, GuavaVersion fromVersion, Class<?>... stubHolders) {
        for (Class<?> holder : stubHolders) {
            GuavaStubs stubsFor = holder.getAnnotation(GuavaStubs.class);
            if (stubsFor == null) {
                throw new IllegalStateException(holder.getName() + " is missing @GuavaStubs naming the class it replaces members of");
            }

            for (Method method : holder.getDeclaredMethods()) {
                GuavaStub stub = method.getAnnotation(GuavaStub.class);
                if (stub == null) continue;
                if (!stub.introducedIn().isEmpty() && GuavaVersion.parse(stub.introducedIn()).isNewerThan(fromVersion)) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                int leadingReceiverParams = stub.staticOriginal() ? 0 : 1;
                String originalDescriptor = MethodDescriptors.descriptorOf(
                        Arrays.copyOfRange(parameterTypes, leadingReceiverParams, parameterTypes.length),
                        method.getReturnType()
                );

                visitorInfos.registerMethodInvocation(
                        stubsFor.value(),
                        method.getName(),
                        originalDescriptor,
                        VisitorInfos.classMember(
                                holder.getName().replace('.', '/'),
                                method.getName(),
                                MethodDescriptors.descriptorOf(method),
                                Modifier.isStatic(method.getModifiers())
                        )
                );
            }
        }
    }

    /**
     * Registers a {@link MappingBuilder#addMapping(String, String)} class rename for
     * every {@link GuavaAdapter}-annotated class in {@code adapters}, redirecting every
     * reference to the original type - not just one member's call sites - onto the
     * adapter, skipping any whose target was introduced later than {@code fromVersion}.
     */
    public static void registerAdapters(MappingBuilder mappingBuilder, GuavaVersion fromVersion, Class<?>... adapters) {
        for (Class<?> adapterHolder : adapters) {
            GuavaAdapter adapter = adapterHolder.getAnnotation(GuavaAdapter.class);
            if (adapter == null) {
                throw new IllegalStateException(adapterHolder.getName() + " is missing @GuavaAdapter naming the class it replaces");
            }
            if (!adapter.introducedIn().isEmpty() && GuavaVersion.parse(adapter.introducedIn()).isNewerThan(fromVersion)) {
                continue;
            }

            mappingBuilder.addMapping(adapter.value(), adapterHolder.getName().replace('.', '/'));
        }
    }
}
