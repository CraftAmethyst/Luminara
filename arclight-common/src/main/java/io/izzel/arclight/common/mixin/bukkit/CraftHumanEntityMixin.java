package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Field;
@Mixin(value = CraftHumanEntity.class)
public abstract class CraftHumanEntityMixin extends CraftEntity {

    // @formatter:off
    @Shadow(remap = false) public abstract Player getHandle();
    @Shadow(remap = false) public abstract void setHandle(Player entity);
    // @formatter:on

    public CraftHumanEntityMixin(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Decorate(method = "getOpenInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getBukkitView()Lorg/bukkit/inventory/InventoryView;"))
    private InventoryView arclight$capturePlayer(AbstractContainerMenu instance) throws Throwable {
        Player handle = getHandle();
        try {
            ArclightCaptures.captureContainerOwner(handle);
            return (InventoryView) DecorationOps.callsite().invoke(instance);
        } finally {
            ArclightCaptures.popContainerOwner(handle);
        }
    }

    @Override
    public void setHandle(Entity entity) {
        setHandle((Player) entity);
    }

    public InventoryView openAnvil(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.ANVIL);
    }

    public InventoryView openCartographyTable(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.CARTOGRAPHY_TABLE);
    }

    public InventoryView openGrindstone(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.GRINDSTONE);
    }

    public InventoryView openLoom(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.LOOM);
    }

    public InventoryView openSmithingTable(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.SMITHING_TABLE);
    }

    public InventoryView openStonecutter(Location location, boolean force) {
        return this.arclight$openInventory(location, force, Material.STONECUTTER);
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
        net.minecraft.world.level.block.Block block = switch (material) {
            case ANVIL -> Blocks.ANVIL;
            case CARTOGRAPHY_TABLE -> Blocks.CARTOGRAPHY_TABLE;
            case GRINDSTONE -> Blocks.GRINDSTONE;
            case LOOM -> Blocks.LOOM;
            case SMITHING_TABLE -> Blocks.SMITHING_TABLE;
            case STONECUTTER -> Blocks.STONECUTTER;
            default -> throw new IllegalArgumentException("Unsupported inventory type: " + material);
        };
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        handle.openMenu(block.getMenuProvider(null, handle.level(), position));
        this.arclight$setCheckReachable(handle, !force);
        return ((AbstractContainerMenuBridge) handle.containerMenu).bridge$getBukkitView();
    }

    private void arclight$setCheckReachable(ServerPlayer handle, boolean checkReachable) {
        try {
            Field field = handle.containerMenu.getClass().getField("checkReachable");
            field.setBoolean(handle.containerMenu, checkReachable);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
