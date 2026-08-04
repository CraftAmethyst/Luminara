package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
@LoadIfMod(modid = ModIds.APOTHEOSIS, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class RepairCostMixin {

    @ModifyConstant(
        method = "createResult",
        constant = @Constant(intValue = 40),
        require = 0
    )
    private int arclight$maximumRepairCost(int raw) {
        return raw;
    }

    @ModifyConstant(
        method = "createResult",
        constant = @Constant(intValue = 39),
        require = 0
    )
    private int arclight$maximumRenameCost(int raw) {
        return raw;
    }
}
