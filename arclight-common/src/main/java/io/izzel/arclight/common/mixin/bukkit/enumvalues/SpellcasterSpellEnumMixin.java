package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.bukkit.entity.Spellcaster.Spell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Spell.class, remap = false)
public class SpellcasterSpellEnumMixin {

    @Mutable
    @Shadow
    private static Spell[] ENUM$VALUES;
}