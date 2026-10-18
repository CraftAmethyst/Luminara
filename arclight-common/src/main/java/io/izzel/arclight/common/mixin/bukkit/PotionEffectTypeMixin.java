package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectType$Category;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(value = PotionEffectType.class, remap = false)
public class PotionEffectTypeMixin {

    @Shadow
    @Final
    private static PotionEffectType[] byId;

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    @NotNull
    public static PotionEffectType[] values() {
        int from = byId[0] == null ? 1 : 0;
        int to = byId[byId.length - 1] == null ? byId.length - 1 : byId.length;
        return Arrays.copyOfRange(byId, from, to);
    }

    @NotNull
    public PotionEffectType$Category getEffectCategory() {
        PotionEffectType effectType = (PotionEffectType) (Object) this;
        while (effectType instanceof PotionEffectTypeWrapper wrapper) {
            effectType = wrapper.getType();
        }
        if (effectType instanceof CraftPotionEffectType craftType) {
            MobEffectCategory category = craftType.getHandle().getCategory();
            return switch (category) {
                case BENEFICIAL -> PotionEffectType$Category.BENEFICIAL;
                case HARMFUL -> PotionEffectType$Category.HARMFUL;
                case NEUTRAL -> PotionEffectType$Category.NEUTRAL;
            };
        }
        throw new UnsupportedOperationException(
            "Cannot determine category for " + effectType.getClass().getName()
        );
    }
}
