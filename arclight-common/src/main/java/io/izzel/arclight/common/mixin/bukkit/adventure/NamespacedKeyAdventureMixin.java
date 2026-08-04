package io.izzel.arclight.common.mixin.bukkit.adventure;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NamespacedKey.class, remap = false)
public abstract class NamespacedKeyAdventureMixin
    implements net.kyori.adventure.key.Key {

    @Shadow
    public abstract @NotNull String getNamespace();

    @Shadow
    public abstract @NotNull String getKey();

    @Override
    public @NotNull String namespace() {
        return getNamespace();
    }

    @Override
    public @NotNull String value() {
        return getKey();
    }

    @Override
    public @NotNull String asString() {
        return getNamespace() + ':' + getKey();
    }

    @Override
    public int hashCode() {
        int result = namespace().hashCode();
        result = (31 * result) + value().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof net.kyori.adventure.key.Key key)) return false;
        return (
            namespace().equals(key.namespace()) && value().equals(key.value())
        );
    }
}
