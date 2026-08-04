package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.PaperAdventure;
import org.bukkit.Nameable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Nameable.class, remap = false)
public interface NameableAdventureMixin {
    @Shadow
    @Nullable
    String getCustomName();

    @Shadow
    void setCustomName(@Nullable String name);

    default net.kyori.adventure.text.@Nullable Component customName() {
        String name = getCustomName();
        return name == null ? null : PaperAdventure.legacyToAdventure(name);
    }

    default void customName(
        final net.kyori.adventure.text.@Nullable Component customName
    ) {
        setCustomName(
            customName == null
                ? null
                : PaperAdventure.adventureToLegacy(customName)
        );
    }
}
