package io.izzel.arclight.common.mixin.core.advancements;

import io.izzel.arclight.common.bridge.core.advancement.AdvancementBridge;
import net.minecraft.advancements.Advancement;
import org.bukkit.craftbukkit.v.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Advancement.class)
public class AdvancementMixin implements AdvancementBridge {

    public final org.bukkit.advancement.Advancement bukkit =
        new CraftAdvancement((Advancement) (Object) this);

    @Override
    public org.bukkit.advancement.Advancement bridge$getBukkit() {
        return bukkit;
    }
}
