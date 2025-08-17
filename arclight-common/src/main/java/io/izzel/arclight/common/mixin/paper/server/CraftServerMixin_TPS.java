package io.izzel.arclight.common.mixin.paper.server;

import org.bukkit.craftbukkit.v.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;

/**
 * Paper API patch 0013: Add getTPS method
 * Implements getTPS() method for CraftServer to expose server TPS information
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_TPS {

    @Shadow
    @Final
    protected net.minecraft.server.dedicated.DedicatedServer console;

    /**
     * Gets the current server TPS
     *
     * @return current server TPS (1m, 5m, 15m in Paper-Server)
     */
    @NotNull
    public double[] getTPS() {
        try {
            // Access the recentTps field from MinecraftServerMixin
            Field recentTpsField = console.getClass().getDeclaredField("recentTps");
            recentTpsField.setAccessible(true);
            double[] recentTps = (double[]) recentTpsField.get(console);

            // Return a copy to prevent external modification
            return new double[]{
                    Math.min(recentTps[0], 20.0), // 1 minute
                    Math.min(recentTps[1], 20.0), // 5 minutes
                    Math.min(recentTps[2], 20.0)  // 15 minutes
            };
        } catch (Exception e) {
            // Fallback to default values if reflection fails
            return new double[]{20.0, 20.0, 20.0};
        }
    }
}
