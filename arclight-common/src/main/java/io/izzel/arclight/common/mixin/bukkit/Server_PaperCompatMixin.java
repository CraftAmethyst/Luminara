package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.adventure.PaperAdventure;
import io.izzel.arclight.common.mod.ArclightConstants;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Server.class, remap = false)
public interface Server_PaperCompatMixin {

    // Paper API compatibility: Server#getCurrentTick()
    default int getCurrentTick() {
        return ArclightConstants.currentTick;
    }

    // Paper API compatibility: Server#createInventory(InventoryHolder, int, Component)
    default Inventory createInventory(InventoryHolder owner, int size, net.kyori.adventure.text.Component title) throws IllegalArgumentException {
        return ((Server) this).createInventory(owner, size, PaperAdventure.adventureToLegacy(title));
    }
}
