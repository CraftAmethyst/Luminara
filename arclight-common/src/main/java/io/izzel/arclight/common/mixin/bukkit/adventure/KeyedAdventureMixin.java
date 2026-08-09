package io.izzel.arclight.common.mixin.bukkit.adventure;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Keyed.class, remap = false)
public interface KeyedAdventureMixin extends net.kyori.adventure.key.Keyed {
    @Shadow
    @NotNull
    NamespacedKey getKey();

    @Override
    default net.kyori.adventure.key.@NotNull Key key() {
        NamespacedKey namespacedKey = getKey();
        return net.kyori.adventure.key.Key.key(
            namespacedKey.getNamespace(),
            namespacedKey.getKey()
        );
    }
}
