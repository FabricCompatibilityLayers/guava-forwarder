package io.github.fabriccompatibilitylayers.guavaforwarder.g30_0_jre.stubs;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ServiceManager;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

@GuavaStubs("com/google/common/util/concurrent/ServiceManager")
public class U_C_ServiceManager {
    @GuavaStub(introducedIn = "15.0", staticOriginal = false)
    public static void addListener(ServiceManager manager, ServiceManager.Listener listener) {
        manager.addListener(listener, MoreExecutors.directExecutor());
    }
}
