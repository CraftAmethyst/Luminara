package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.compat.PortalForcerSearchRadiusAccess;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PortalForcer.class)
@LoadIfMod(modid = ModIds.RADIUM, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class PortalForcerSearchRadiusMixin
    implements PortalForcerSearchRadiusAccess
{

    private transient int arclight$searchRadius = -1;

    @Shadow
    public abstract Optional<BlockUtil.FoundRectangle> findPortalAround(
        BlockPos pos,
        boolean destinationIsNether,
        WorldBorder worldBorder
    );

    @ModifyArg(
        method = "m_192985_",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;m_27056_(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;I)V",
            remap = false
        ),
        index = 2,
        remap = false
    )
    private int arclight$useSearchRadius(int original) {
        return this.arclight$searchRadius == -1
            ? original
            : this.arclight$searchRadius;
    }

    @Override
    public Optional<BlockUtil.FoundRectangle> arclight$findPortalAround(
        BlockPos pos,
        WorldBorder worldBorder,
        int searchRadius
    ) {
        this.arclight$searchRadius = searchRadius;
        try {
            return this.findPortalAround(pos, false, worldBorder);
        } finally {
            this.arclight$searchRadius = -1;
        }
    }
}
