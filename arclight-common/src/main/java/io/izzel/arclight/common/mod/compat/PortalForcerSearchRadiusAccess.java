package io.izzel.arclight.common.mod.compat;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Optional;

public interface PortalForcerSearchRadiusAccess {
    Optional<BlockUtil.FoundRectangle> arclight$findPortalAround(
        BlockPos pos,
        WorldBorder worldBorder,
        int searchRadius
    );
}
