package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HumanEntity.class, remap = false)
public interface HumanEntity_PaperCompatMixin {
    InventoryView openAnvil(Location location, boolean force);

    InventoryView openCartographyTable(Location location, boolean force);

    InventoryView openGrindstone(Location location, boolean force);

    InventoryView openLoom(Location location, boolean force);

    InventoryView openSmithingTable(Location location, boolean force);

    InventoryView openStonecutter(Location location, boolean force);
}
