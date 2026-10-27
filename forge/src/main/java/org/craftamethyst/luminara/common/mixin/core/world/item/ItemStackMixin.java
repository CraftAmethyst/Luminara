package org.craftamethyst.luminara.common.mixin.core.world.item;

import org.craftamethyst.luminara.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import org.craftamethyst.luminara.common.bridge.core.item.ItemStackBridge;
import org.craftamethyst.luminara.common.mod.util.log.ArclightI18nLogger;
import org.craftamethyst.luminara.i18n.ArclightConfig;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackBridge {

  private static final Logger LOG = ArclightI18nLogger.getLogger("ItemStack");

  // @formatter:off
  @Shadow @Deprecated private Item item;

  @Shadow private int count;

  // @formatter:on
  @Mutable
  @Shadow(remap = false)
  @Final
  private net.minecraft.core.Holder.Reference<Item> delegate;

  @Redirect(
      method = "isSameItemSameTags",
      at =
          @At(
              value = "INVOKE",
              remap = false,
              target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
  private static boolean arclight$lenientItemMatch(Object a, Object b) {
    if (ArclightConfig.spec().getCompat().isLenientItemTagMatch()) {
      var tagA = (CompoundTag) a;
      var tagB = (CompoundTag) b;
      if (tagB != null) {
        var tmp = tagA;
        tagA = tagB;
        tagB = tmp;
      }
      return (tagA == null
          || (tagA.isEmpty() ? tagB == null || tagB.isEmpty() : tagA.equals(tagB)));
    } else {
      return Objects.equals(a, b);
    }
  }

  private static final java.lang.reflect.Method SERIALIZE_CAPS;
  private static final java.lang.reflect.Method DESERIALIZE_CAPS;

  static {
    try {
      SERIALIZE_CAPS =
          CapabilityProvider.class.getDeclaredMethod("serializeCaps");
      SERIALIZE_CAPS.setAccessible(true);
      DESERIALIZE_CAPS =
          CapabilityProvider.class.getDeclaredMethod("deserializeCaps", CompoundTag.class);
      DESERIALIZE_CAPS.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @Override
  public CompoundTag bridge$getForgeCaps() {
    try {
      return (CompoundTag) SERIALIZE_CAPS.invoke(this);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot serialize Forge capabilities", e);
    }
  }

  @Override
  public void bridge$setForgeCaps(CompoundTag caps) {
    if (caps != null) {
      try {
        DESERIALIZE_CAPS.invoke(this, caps);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Cannot deserialize Forge capabilities", e);
      }
    }
  }

  public void convertStack(int version) {
    if (0 < version && version < CraftMagicNumbers.INSTANCE.getDataVersion()) {
      LOG.warn("i18n.legacy-itemstack", this);
    }
  }

  @Override
  public void bridge$convertStack(int version) {
    this.convertStack(version);
  }

  @ModifyVariable(
      method = "hurt",
      index = 1,
      at = @At(value = "JUMP", opcode = Opcodes.IFGT, ordinal = 0))
  private int arclight$itemDamage(int i, int amount, RandomSource rand, ServerPlayer damager) {
    if (damager != null) {
      PlayerItemDamageEvent event =
          new PlayerItemDamageEvent(
              ((ServerPlayerEntityBridge) damager).bridge$getBukkitEntity(),
              CraftItemStack.asCraftMirror((ItemStack) (Object) this),
              i);
      event.getPlayer().getServer().getPluginManager().callEvent(event);

      if (i != event.getDamage() || event.isCancelled()) {
        event.getPlayer().updateInventory();
      }
      if (event.isCancelled()) {
        return -1;
      }
      return event.getDamage();
    }
    return i;
  }

  @Inject(
      method = "hurtAndBreak",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
  private <T extends LivingEntity> void arclight$itemBreak(
      int amount, T entityIn, Consumer<T> onBroken, CallbackInfo ci) {
    if (this.count == 1 && entityIn instanceof Player) {
      CraftEventFactory.callPlayerItemBreakEvent((Player) entityIn, (ItemStack) (Object) this);
    }
  }

  @Deprecated
  public void setItem(Item item) {
    this.item = item;
    this.delegate = ForgeRegistries.ITEMS.getDelegateOrThrow(item);
  }
}
