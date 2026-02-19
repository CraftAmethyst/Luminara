package io.izzel.arclight.common.mixin.core.world.entity.player;

import com.mojang.datafixers.util.Either;
import io.izzel.arclight.common.adventure.PaperAdventure;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.network.play.ServerPlayNetHandlerBridge;
import io.izzel.arclight.common.mixin.core.world.entity.LivingEntityMixin_FabricBridge;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(ServerPlayer.class)
@LoadIfMod(modid = {"forge"}, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class ServerPlayerMixin_FabricBridge extends LivingEntityMixin_FabricBridge implements ServerPlayerEntityBridge {

    // @formatter:off
    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow public int lastSentExp;
    @Unique
    private transient PlayerTeleportEvent.TeleportCause arclight$teleportCause;
    @Unique
    private transient PlayerSpawnChangeEvent.Cause arclight$spawnCause;
    @Unique
    private transient EntityExhaustionEvent.ExhaustionReason arclight$exhaustReason;
    @Unique
    private Location arclight$compassTarget;
    @Unique
    private boolean arclight$trackerDirty;
    @Unique
    private boolean arclight$joining = true;
    @Unique
    private boolean arclight$initialized;
    @Unique
    private boolean arclight$fauxSleeping;

    @Shadow public abstract void setServerLevel(ServerLevel world);
    @Shadow public abstract void triggerDimensionChangeTriggers(ServerLevel world);
    @Shadow @Nullable public abstract BlockPos getRespawnPosition();
    @Shadow public abstract ResourceKey<Level> getRespawnDimension();
    // @formatter:on

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$fabricInit(MinecraftServer server, ServerLevel world, com.mojang.authlib.GameProfile profile, CallbackInfo ci) {
        this.arclight$initialized = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void arclight$fabricJoinState(CallbackInfo ci) {
        if (this.arclight$joining) {
            this.arclight$joining = false;
        }
    }

    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) internal$getBukkitEntity();
    }

    @Override
    public CraftPlayer bridge$getBukkitEntity() {
        return (CraftPlayer) internal$getBukkitEntity();
    }

    @Override
    public boolean bridge$isFauxSleeping() {
        return this.arclight$fauxSleeping;
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> bridge$trySleep(BlockPos at, boolean force) {
        return ((ServerPlayer) (Object) this).startSleepInBed(at);
    }

    @Override
    public void bridge$pushExhaustReason(EntityExhaustionEvent.ExhaustionReason reason) {
        this.arclight$exhaustReason = reason;
    }

    @Override
    public float bridge$getAttackCooldown() {
        return ((Player) (Object) this).getAttackStrengthScale(0.5f);
    }

    @Override
    public void bridge$resetAttackCooldown() {
        ((Player) (Object) this).resetAttackStrengthTicker();
    }

    @Override
    public Location bridge$getCompassTarget() {
        if (this.arclight$compassTarget != null) {
            return this.arclight$compassTarget;
        }
        var respawnPos = this.getRespawnPosition();
        var respawnDim = this.getRespawnDimension();
        if (respawnPos != null && respawnDim != null) {
            var world = this.server.getLevel(respawnDim);
            if (world != null) {
                return new Location(bridge$getBukkitEntity().getWorld(), respawnPos.getX(), respawnPos.getY(), respawnPos.getZ());
            }
        }
        return bridge$getBukkitEntity().getWorld().getSpawnLocation();
    }

    @Override
    public void bridge$pushChangeDimensionCause(PlayerTeleportEvent.TeleportCause cause) {
        this.arclight$teleportCause = cause;
    }

    @Override
    public void bridge$pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause cause) {
        this.arclight$spawnCause = cause;
    }

    @Override
    public Optional<PlayerTeleportEvent.TeleportCause> bridge$getTeleportCause() {
        try {
            return Optional.ofNullable(this.arclight$teleportCause);
        } finally {
            this.arclight$teleportCause = null;
        }
    }

    @Override
    public BlockPos bridge$getSpawnPoint(ServerLevel world) {
        return world.getSharedSpawnPos();
    }

    @Override
    public boolean bridge$isMovementBlocked() {
        var self = (ServerPlayer) (Object) this;
        return !self.isAlive() || self.isSleeping() || self.isRemoved();
    }

    @Override
    public void bridge$setCompassTarget(Location location) {
        this.arclight$compassTarget = location;
    }

    @Override
    public boolean bridge$isJoining() {
        return this.arclight$joining;
    }

    @Override
    public void bridge$reset() {
        var self = (ServerPlayer) (Object) this;
        self.setHealth(self.getMaxHealth());
        self.stopUsingItem();
        self.setRemainingFireTicks(0);
        self.resetFallDistance();
        self.setArrowCount(0);
        self.removeAllEffects();
        this.lastSentExp = -1;
        self.setDeltaMovement(0.0, 0.0, 0.0);
    }

    @Override
    public Entity bridge$changeDimension(ServerLevel world, PlayerTeleportEvent.TeleportCause cause) {
        this.arclight$teleportCause = cause;
        return ((ServerPlayer) (Object) this).changeDimension(world);
    }

    @Override
    public boolean bridge$initialized() {
        return this.arclight$initialized;
    }

    @Override
    public boolean bridge$isTrackerDirty() {
        return this.arclight$trackerDirty;
    }

    @Override
    public void bridge$setTrackerDirty(boolean flag) {
        this.arclight$trackerDirty = flag;
    }

    @Override
    public void bridge$sendActionBar(net.kyori.adventure.text.Component message) {
        var vanilla = PaperAdventure.asVanilla(message);
        this.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(vanilla));
    }

    @Override
    public void bridge$sendTitle(net.kyori.adventure.title.Title title) {
        var times = title.times();
        if (times != null) {
            this.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(
                    (int) (times.fadeIn().toMillis() / 50L),
                    (int) (times.stay().toMillis() / 50L),
                    (int) (times.fadeOut().toMillis() / 50L)
            ));
        }
        this.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(title.title())));
        this.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(title.subtitle())));
    }

    @Override
    public int bridge$getPing() {
        return ((ServerPlayNetHandlerBridge) this.connection).bridge$getLatency();
    }

    @Override
    public void bridge$updateCommands() {
        this.server.getCommands().sendCommands((ServerPlayer) (Object) this);
    }
}
