package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.i18n.ArclightConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = SimpleCommandMap.class, remap = false)
public abstract class SimpleCommandMapMixin {

    @Shadow
    protected Map<String, Command> knownCommands;

    @Inject(method = "setDefaultCommands", at = @At("TAIL"))
    private void arclight$maybeRemoveReload(CallbackInfo ci) {
        if (ArclightConfig.spec().getCompat().isEnableBukkitReloadCommand()) {
            return;
        }
        Command command = this.knownCommands.remove("reload");
        this.knownCommands.remove("bukkit:reload");
        if (command != null) {
            command.unregister((CommandMap) this);
        }
    }
}
