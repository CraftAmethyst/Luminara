package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.translation.Translator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Locale;

@Mixin(value = Player.class, remap = false)
public interface PlayerLocaleMixin {
    @NotNull
    default Locale locale() {
        String locale = ((Player) this).getLocale();
        if (locale == null || locale.isEmpty()) {
            return Locale.US;
        }
        return Translator.parseLocale(locale);
    }
}
