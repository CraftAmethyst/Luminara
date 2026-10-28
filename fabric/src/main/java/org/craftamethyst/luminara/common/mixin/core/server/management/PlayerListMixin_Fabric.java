package org.craftamethyst.luminara.common.mixin.core.server.management;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.craftamethyst.luminara.common.mod.server.ArclightServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin_Fabric {

  @Inject(method = "<init>", at = @At("RETURN"))
  private void arclight$loadServer(
      MinecraftServer minecraftServer,
      LayeredRegistryAccess<RegistryLayer> registries,
      PlayerDataStorage playerDataStorage,
      int maxPlayers,
      CallbackInfo ci) {
    if (minecraftServer instanceof DedicatedServer dedicatedServer) {
      ArclightServer.createOrLoad(dedicatedServer, (PlayerList) (Object) this);
    }
  }
}
