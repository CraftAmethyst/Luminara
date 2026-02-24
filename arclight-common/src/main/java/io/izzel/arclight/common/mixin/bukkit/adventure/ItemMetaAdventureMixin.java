package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = ItemMeta.class, remap = false)
public interface ItemMetaAdventureMixin {

    @Shadow
    boolean hasDisplayName();

    @Shadow
    @Nullable String getDisplayName();

    @Shadow
    void setDisplayName(@Nullable String name);

    @Shadow
    boolean hasLore();

    @Shadow
    @Nullable List<String> getLore();

    @Shadow
    void setLore(@Nullable List<String> lore);

    default @Nullable Component displayName() {
        if (!hasDisplayName()) {
            return null;
        }
        String displayName = getDisplayName();
        return displayName == null ? null : PaperAdventure.legacyToAdventure(displayName);
    }

    default void displayName(final @Nullable Component displayName) {
        setDisplayName(displayName == null ? null : PaperAdventure.adventureToLegacy(displayName));
    }

    default @Nullable List<Component> lore() {
        if (!hasLore()) {
            return null;
        }
        List<String> lore = getLore();
        if (lore == null) {
            return null;
        }
        return lore.stream()
                .map(line -> line == null ? null : PaperAdventure.legacyToAdventure(line))
                .toList();
    }

    default void lore(final @Nullable List<? extends Component> lore) {
        if (lore == null) {
            setLore(null);
            return;
        }
        setLore(lore.stream()
                .map(line -> line == null ? null : PaperAdventure.adventureToLegacy(line))
                .toList());
    }
}
