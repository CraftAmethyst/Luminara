package io.izzel.arclight.common.mixin.paper.server;

import io.papermc.paper.configuration.PaperConfigurations;
import io.papermc.paper.configuration.WorldConfiguration;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerLevel fixes for Paper compatibility in Luminara.
 * This mixin adds Paper configuration access to ServerLevel.
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin_CBFixes {

    /**
     * Initialize Paper world configuration when the level is created.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$initializePaperWorldConfig(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        PaperConfigurations.initializeWorldConfiguration(level);
    }

    /**
     * Cleanup Paper world configuration when the level is closed.
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void arclight$cleanupPaperWorldConfig(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        PaperConfigurations.cleanupWorldConfiguration(level.dimension().location());
    }

    /**
     * Provides access to Paper world configuration.
     * This method will be called by Paper API to get world-specific configuration.
     */
    public WorldConfiguration paperConfig() {
        ServerLevel level = (ServerLevel) (Object) this;
        return PaperConfigurations.getWorldConfiguration(level);
    }
}
