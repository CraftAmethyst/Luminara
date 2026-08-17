package io.izzel.arclight.fabric;

import io.izzel.arclight.api.Arclight;
import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.common.mod.boot.AbstractBootstrap;
import io.izzel.arclight.fabric.mod.FabricArclightServer;
import io.izzel.arclight.fabric.mod.event.EventHandlerRegistry;
import io.izzel.arclight.fabric.mod.permission.ArclightPermissionImpl;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Runs through the dedicated-server entrypoint type, so Bukkit is never initialized on
 * a client or integrated-server environment (same guarantee as the NeoForge
 * {@code Dist.DEDICATED_SERVER} guard).
 */
public class ArclightModEntrypoint implements DedicatedServerModInitializer, AbstractBootstrap {

    @Override
    public void onInitializeServer() {
        try {
            // Keep the standalone Fabric path on the same version/platform bootstrap as
            // NeoForge. The connector establishes these values early for mixin loading;
            // setupMod makes the entrypoint safe when invoked in isolation as well.
            this.setupMod(ArclightPlatform.FABRIC);
            this.installGsonEnumFactory();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to apply Fabric Arclight bootstrap", t);
        }
        Arclight.setServer(new FabricArclightServer());
        EventHandlerRegistry.register();
        ArclightPermissionImpl.init();
        AbstractBootstrap.setPlatformIfAbsent(ArclightPlatform.FABRIC);
    }
}