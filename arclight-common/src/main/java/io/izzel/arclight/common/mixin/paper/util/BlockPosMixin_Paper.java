package io.izzel.arclight.common.mixin.paper.util;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

/**
 * BlockPos fixes for Paper compatibility in Luminara.
 * This mixin fixes decompilation issues in BlockPos iterator from Paper patch 0006.
 */
@Mixin(BlockPos.class)
public class BlockPosMixin_Paper {

    /**
     * Paper patch 0006 MC-Dev fixes.
     * This class represents the fixes applied to BlockPos for better decompilation.
     * The actual fixes are applied at the source level during the MC-Dev process.
     * This mixin serves as a marker for Paper compatibility.
     */
    // Note: The actual MC-Dev fixes are applied during the decompilation process
    // This includes fixing variable naming conflicts in iterators and other decompilation issues
}
