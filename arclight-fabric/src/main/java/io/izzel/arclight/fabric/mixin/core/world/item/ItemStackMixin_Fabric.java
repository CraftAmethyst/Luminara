package io.izzel.arclight.fabric.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.item.ItemStackBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_Fabric implements ItemStackBridge {

    @Unique
    private CompoundTag arclight$forgeCaps;

    @Override
    public void bridge$convertStack(int version) {
        // Fabric has no Forge data fixer path for forgeCaps.
    }

    @Override
    public CompoundTag bridge$getForgeCaps() {
        return this.arclight$forgeCaps;
    }

    @Override
    public void bridge$setForgeCaps(CompoundTag caps) {
        this.arclight$forgeCaps = caps;
    }
}
