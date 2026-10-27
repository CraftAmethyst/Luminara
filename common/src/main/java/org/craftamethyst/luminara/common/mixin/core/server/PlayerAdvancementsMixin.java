package org.craftamethyst.luminara.common.mixin.core.server;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.craftamethyst.luminara.common.bridge.core.advancement.AdvancementBridge;
import org.craftamethyst.luminara.common.bridge.core.entity.EntityBridge;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

  @Shadow private ServerPlayer player;

  @Shadow
  public abstract AdvancementProgress getOrStartProgress(Advancement advancement);

  @Inject(
      method = "award",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/advancements/Advancement;getRewards()Lnet/minecraft/advancements/AdvancementRewards;"))
  public void arclight$callEvent(
      Advancement advancementIn, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
    Bukkit.getPluginManager()
        .callEvent(
            new org.bukkit.event.player.PlayerAdvancementDoneEvent(
                (Player) ((EntityBridge) this.player).bridge$getBukkitEntity(),
                ((AdvancementBridge) advancementIn).bridge$getBukkit()));
  }

  @Inject(
      method = "award",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/PlayerAdvancements;unregisterListeners(Lnet/minecraft/advancements/Advancement;)V"),
      cancellable = true)
  public void arclight$callCriterionGrantEvent(
      Advancement advancementIn, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
    PlayerAdvancementCriterionGrantEvent event =
        new PlayerAdvancementCriterionGrantEvent(
            (Player) ((EntityBridge) this.player).bridge$getBukkitEntity(),
            ((AdvancementBridge) advancementIn).bridge$getBukkit(),
            criterionKey);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      this.getOrStartProgress(advancementIn).revokeProgress(criterionKey);
      cir.setReturnValue(false);
    }
  }
}
