package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.inventory.container.ContainerBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = CraftHumanEntity.class, remap = false)
public abstract class CraftHumanEntityMixin extends CraftEntity {

    // @formatter:off
    @Shadow private CraftInventoryPlayer inventory;
    public CraftHumanEntityMixin(CraftServer server, Entity entity) {
        super(server, entity);
    }
    // @formatter:on

    @Shadow
    public abstract Player getHandle();

    @Override
    public void setHandle(Entity entity) {
        super.setHandle(entity);
        this.inventory = new CraftInventoryPlayer(((Player) entity).getInventory());
    }

    @Inject(method = "getOpenInventory", at = @At("HEAD"))
    private void arclight$capturePlayer(CallbackInfoReturnable<InventoryView> cir) {
        ArclightCaptures.captureContainerOwner(this.getHandle());
    }

    @Inject(method = "getOpenInventory", at = @At("RETURN"))
    private void arclight$resetPlayer(CallbackInfoReturnable<InventoryView> cir) {
        ArclightCaptures.resetContainerOwner();
    }

    public InventoryView openAnvil(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.ANVIL);
    }

    public InventoryView openCartographyTable(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.CARTOGRAPHY_TABLE);
    }

    private InventoryView arclight$openInventory(Location location, boolean force, Material material) {
        org.spigotmc.AsyncCatcher.catchOp("open" + material);
        if (!(this.getHandle() instanceof ServerPlayer handle)) {
            return null;
        }
        if (location == null) {
            location = this.getLocation();
        }
        if (!force) {
            Block blockAtLocation = location.getBlock();
            if (blockAtLocation.getType() != material) {
                return null;
            }
        }
        net.minecraft.world.level.block.Block block;
        if (material == Material.ANVIL) {
            block = Blocks.ANVIL;
        } else if (material == Material.CARTOGRAPHY_TABLE) {
            block = Blocks.CARTOGRAPHY_TABLE;
        } else {
            throw new IllegalArgumentException("Unsupported inventory type: " + material);
        }
        handle.openMenu(block.getMenuProvider(null, handle.level(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        this.arclight$setCheckReachable(handle, !force);
        return ((ContainerBridge) handle.containerMenu).bridge$getBukkitView();
    }

    private void arclight$setCheckReachable(ServerPlayer handle, boolean checkReachable) {
        try {
            Field field = handle.containerMenu.getClass().getField("checkReachable");
            field.setBoolean(handle.containerMenu, checkReachable);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
