package io.github.fabriccompatibilitylayers.guavaforwarder.g13_0;

import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubRegistrar;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersion;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaVersionModule;
import io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs.I_ByteStreams;
import io.github.fabriccompatibilitylayers.guavaforwarder.g13_0.stubs.I_Files;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

public class GuavaVersionModuleImpl implements GuavaVersionModule {
    private static final GuavaVersion INPUT_SUPPLIER_REMOVED_IN = GuavaVersion.parse("20.0");

    @Override
    public void registerVisitors(VisitorInfos visitorInfos, GuavaVersion fromVersion, GuavaVersion toVersion) {
        GuavaStubRegistrar.register(visitorInfos, fromVersion, I_Files.class);

        // I_ByteStreams#getDigest takes a leading InputSupplier parameter (to match the
        // original member's descriptor) - InputSupplier itself was removed in Guava
        // 20.0, so once the actual runtime Guava is that new, no mod bytecode could hold
        // an InputSupplier instance to call this with anyway, and reflecting over the
        // stub class would fail trying to resolve that removed type.
        if (toVersion.isOlderThan(INPUT_SUPPLIER_REMOVED_IN)) {
            GuavaStubRegistrar.register(visitorInfos, fromVersion, I_ByteStreams.class);
        }

        visitorInfos.registerMethodInvocation(
                "com/google/common/collect/Iterables",
                "reverse",
                "(Ljava/util/List;)Ljava/lang/Iterable;",
                VisitorInfos.classMember(
                        "com/google/common/collect/Lists",
                        "reverse",
                        "(Ljava/util/List;)Ljava/util/List;",
                        true
                )
        );
    }
}
