package org.bukkit.potion;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public enum PotionEffectType$Category {
  BENEFICIAL(NamedTextColor.BLUE),
  HARMFUL(NamedTextColor.RED),
  NEUTRAL(NamedTextColor.BLUE);

  private final TextColor color;

  PotionEffectType$Category(@NotNull TextColor color) {
    this.color = color;
  }

  @NotNull
  public TextColor getColor() {
    return this.color;
  }
}
