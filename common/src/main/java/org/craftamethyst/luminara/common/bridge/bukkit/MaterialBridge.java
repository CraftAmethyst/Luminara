package org.craftamethyst.luminara.common.bridge.bukkit;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftMetaItem;
import org.bukkit.inventory.meta.ItemMeta;
import org.craftamethyst.luminara.i18n.conf.MaterialPropertySpec;

public interface MaterialBridge {
  void bridge$setupBlock(ResourceLocation key, Block block, MaterialPropertySpec spec);

  void bridge$setupVanillaBlock(MaterialPropertySpec spec);

  void bridge$setupItem(ResourceLocation key, Item item, MaterialPropertySpec spec);

  void bridge$setBlock();

  void bridge$setItem();

  @Nullable
  MaterialPropertySpec bridge$getSpec();

  MaterialPropertySpec.MaterialType bridge$getType();

  Function<CraftMetaItem, ItemMeta> bridge$itemMetaFactory();

  void bridge$setItemMetaFactory(Function<CraftMetaItem, ItemMeta> func);

  Function<CraftBlock, BlockState> bridge$blockStateFactory();

  void bridge$setBlockStateFactory(Function<CraftBlock, BlockState> func);

  boolean bridge$shouldApplyStateFactory();
}
