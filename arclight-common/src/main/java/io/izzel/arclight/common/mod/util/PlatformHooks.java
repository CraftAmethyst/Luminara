package io.izzel.arclight.common.mod.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class PlatformHooks {

    private static final boolean FORGE_PRESENT = isClassPresent("net.minecraftforge.server.ServerLifecycleHooks");

    private PlatformHooks() {
    }

    public static boolean isForgePresent() {
        return FORGE_PRESENT;
    }

    public static boolean handleServerLogin(ClientIntentionPacket packet, Connection connection) {
        Object ret = invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "handleServerLogin",
                new Class<?>[]{ClientIntentionPacket.class, Connection.class},
                packet,
                connection
        );
        if (ret instanceof Boolean result) {
            return result;
        }
        return true;
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        Object ret = invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "getCurrentServer",
                new Class<?>[0]
        );
        return ret instanceof MinecraftServer server ? server : null;
    }

    public static void handleServerStarted(MinecraftServer server) {
        invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "handleServerStarted",
                new Class<?>[]{MinecraftServer.class},
                server
        );
    }

    public static void handleServerStopping(MinecraftServer server) {
        invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "handleServerStopping",
                new Class<?>[]{MinecraftServer.class},
                server
        );
    }

    public static void expectServerStopped() {
        invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "expectServerStopped",
                new Class<?>[0]
        );
    }

    public static void handleServerStopped(MinecraftServer server) {
        invokeStatic(
                "net.minecraftforge.server.ServerLifecycleHooks",
                "handleServerStopped",
                new Class<?>[]{MinecraftServer.class},
                server
        );
    }

    public static void postLevelLoad(ServerLevel level) {
        postForgeLevelEvent("net.minecraftforge.event.level.LevelEvent$Load", level);
    }

    public static void postLevelUnload(ServerLevel level) {
        postForgeLevelEvent("net.minecraftforge.event.level.LevelEvent$Unload", level);
    }

    public static void reinstatePersistentChunks(ServerLevel level, ForcedChunksSavedData data) {
        invokeStatic(
                "net.minecraftforge.common.world.ForgeChunkManager",
                "reinstatePersistentChunks",
                new Class<?>[]{ServerLevel.class, ForcedChunksSavedData.class},
                level,
                data
        );
    }

    public static void firePlayerRespawnEvent(ServerPlayer player, boolean conqueredEnd) {
        invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "firePlayerRespawnEvent",
                new Class<?>[]{ServerPlayer.class, boolean.class},
                player,
                conqueredEnd
        );
    }

    public static void firePlayerChangedDimensionEvent(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "firePlayerChangedDimensionEvent",
                new Class<?>[]{ServerPlayer.class, ResourceKey.class, ResourceKey.class},
                player,
                from,
                to
        );
    }

    @Nullable
    public static String processCommandEvent(ParseResults<CommandSourceStack> parse) {
        if (!FORGE_PRESENT) {
            return parse.getReader().getString();
        }
        try {
            Class<?> eventClass = Class.forName("net.minecraftforge.event.CommandEvent");
            Constructor<?> constructor = eventClass.getConstructor(ParseResults.class);
            Object event = constructor.newInstance(parse);
            if (postForgeEvent(event)) {
                return null;
            }
            Method getException = eventClass.getMethod("getException");
            if (getException.invoke(event) != null) {
                return null;
            }
            Method getParseResults = eventClass.getMethod("getParseResults");
            Object result = getParseResults.invoke(event);
            if (result instanceof ParseResults<?> parseResults) {
                return parseResults.getReader().getString();
            }
            return parse.getReader().getString();
        } catch (Throwable t) {
            return parse.getReader().getString();
        }
    }

    public static String getServerBranding() {
        Object branding = invokeStatic(
                "net.minecraftforge.internal.BrandingControl",
                "getServerBranding",
                new Class<?>[0]
        );
        if (branding instanceof String value && !value.isBlank()) {
            return value;
        }
        return "fabric";
    }

    public static int getExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, int exp) {
        Object ret = invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "getExperienceDrop",
                new Class<?>[]{LivingEntity.class, Player.class, int.class},
                entity,
                attackingPlayer,
                exp
        );
        if (ret instanceof Integer value) {
            return value;
        }
        return exp;
    }

    public static boolean postMobEffectExpired(LivingEntity entity, MobEffectInstance effect) {
        if (!FORGE_PRESENT) {
            return false;
        }
        try {
            Class<?> eventClass = Class.forName("net.minecraftforge.event.entity.living.MobEffectEvent$Expired");
            Constructor<?> ctor = eventClass.getConstructor(LivingEntity.class, MobEffectInstance.class);
            Object event = ctor.newInstance(entity, effect);
            return postForgeEvent(event);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void postMobEffectAdded(LivingEntity entity, @Nullable MobEffectInstance oldEffect, MobEffectInstance newEffect, @Nullable Entity source) {
        if (!FORGE_PRESENT) {
            return;
        }
        try {
            Class<?> eventClass = Class.forName("net.minecraftforge.event.entity.living.MobEffectEvent$Added");
            Constructor<?> ctor = eventClass.getConstructor(LivingEntity.class, MobEffectInstance.class, MobEffectInstance.class, Entity.class);
            Object event = ctor.newInstance(entity, oldEffect, newEffect, source);
            postForgeEvent(event);
        } catch (Throwable ignored) {
        }
    }

    public static boolean onLivingAttack(LivingEntity entity, DamageSource source, float amount) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onLivingAttack",
                new Class<?>[]{LivingEntity.class, DamageSource.class, float.class},
                entity,
                source,
                amount
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return true;
    }

    public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onLivingHurt",
                new Class<?>[]{LivingEntity.class, DamageSource.class, float.class},
                entity,
                source,
                amount
        );
        if (ret instanceof Float value) {
            return value;
        }
        return amount;
    }

    public static float onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onLivingDamage",
                new Class<?>[]{LivingEntity.class, DamageSource.class, float.class},
                entity,
                source,
                amount
        );
        if (ret instanceof Float value) {
            return value;
        }
        return amount;
    }

    public static boolean onLivingUseTotem(LivingEntity entity, DamageSource source, ItemStack stack, InteractionHand hand) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onLivingUseTotem",
                new Class<?>[]{LivingEntity.class, DamageSource.class, ItemStack.class, InteractionHand.class},
                entity,
                source,
                stack,
                hand
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return true;
    }

    public static boolean onLivingDeath(ServerPlayer player, DamageSource source) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onLivingDeath",
                new Class<?>[]{LivingEntity.class, DamageSource.class},
                player,
                source
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return false;
    }

    public static boolean onPlayerAttack(Player player, DamageSource source, float amount) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onPlayerAttack",
                new Class<?>[]{Player.class, DamageSource.class, float.class},
                player,
                source,
                amount
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return true;
    }

    public static boolean onPlayerAttackTarget(Player player, Entity target) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onPlayerAttackTarget",
                new Class<?>[]{Player.class, Entity.class},
                player,
                target
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return true;
    }

    @Nullable
    public static Float getCriticalHitDamageModifier(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
        if (!FORGE_PRESENT) {
            return vanillaCritical ? damageModifier : null;
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method method = hooks.getMethod("getCriticalHit", Player.class, Entity.class, boolean.class, float.class);
            Object event = method.invoke(null, player, target, vanillaCritical, damageModifier);
            if (event == null) {
                return null;
            }
            Object modifier = event.getClass().getMethod("getDamageModifier").invoke(event);
            if (modifier instanceof Number value) {
                return value.floatValue();
            }
        } catch (Throwable ignored) {
        }
        return vanillaCritical ? damageModifier : null;
    }

    public static boolean canPerformSweepAttack(ItemStack stack) {
        if (FORGE_PRESENT) {
            try {
                Class<?> toolActions = Class.forName("net.minecraftforge.common.ToolActions");
                Class<?> toolAction = Class.forName("net.minecraftforge.common.ToolAction");
                Object sweepAction = toolActions.getField("SWORD_SWEEP").get(null);
                Method method = ItemStack.class.getMethod("canPerformAction", toolAction);
                Object ret = method.invoke(stack, sweepAction);
                if (ret instanceof Boolean value) {
                    return value;
                }
            } catch (Throwable ignored) {
            }
        }
        return stack.getItem() instanceof SwordItem;
    }

    public static double getEntityReach(Player player) {
        if (FORGE_PRESENT) {
            try {
                Method method = player.getClass().getMethod("getEntityReach");
                Object ret = method.invoke(player);
                if (ret instanceof Number value) {
                    return value.doubleValue();
                }
            } catch (Throwable ignored) {
            }
        }
        return 3.0D;
    }

    public static AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {
        if (FORGE_PRESENT) {
            try {
                Method method = ItemStack.class.getMethod("getSweepHitBox", Player.class, Entity.class);
                Object ret = method.invoke(stack, player, target);
                if (ret instanceof AABB aabb) {
                    return aabb;
                }
            } catch (Throwable ignored) {
            }
        }
        return target.getBoundingBox().inflate(1.0D, 0.25D, 1.0D);
    }

    public static boolean doesSneakBypassUse(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (FORGE_PRESENT) {
            try {
                Method method = ItemStack.class.getMethod("doesSneakBypassUse", Level.class, BlockPos.class, Player.class);
                Object ret = method.invoke(stack, level, pos, player);
                if (ret instanceof Boolean value) {
                    return value;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (FORGE_PRESENT) {
            try {
                Method method = ItemStack.class.getMethod("onItemUseFirst", UseOnContext.class);
                Object ret = method.invoke(stack, context);
                if (ret instanceof InteractionResult result) {
                    return result;
                }
            } catch (Throwable ignored) {
            }
        }
        return InteractionResult.PASS;
    }

    public static Entity getMultipartParent(Entity entity) {
        if (FORGE_PRESENT) {
            try {
                Class<?> partEntity = Class.forName("net.minecraftforge.entity.PartEntity");
                if (partEntity.isInstance(entity)) {
                    Object parent = partEntity.getMethod("getParent").invoke(entity);
                    if (parent instanceof Entity parentEntity) {
                        return parentEntity;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return entity;
    }

    public static void onPlayerDestroyItem(Player player, ItemStack original, InteractionHand hand) {
        invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "onPlayerDestroyItem",
                new Class<?>[]{Player.class, ItemStack.class, InteractionHand.class},
                player,
                original,
                hand
        );
    }

    public static LeftClickBlockResult onLeftClickBlock(ServerPlayer player, BlockPos pos, Direction direction, ServerboundPlayerActionPacket.Action action) {
        if (!FORGE_PRESENT) {
            return LeftClickBlockResult.DEFAULT;
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method method = hooks.getMethod("onLeftClickBlock", Player.class, BlockPos.class, Direction.class, ServerboundPlayerActionPacket.Action.class);
            Object event = method.invoke(null, player, pos, direction, action);
            if (event == null) {
                return LeftClickBlockResult.DEFAULT;
            }
            Class<?> type = event.getClass();
            boolean cancelled = Boolean.TRUE.equals(type.getMethod("isCanceled").invoke(event));
            EventDecision useItem = mapEventDecision(type.getMethod("getUseItem").invoke(event));
            EventDecision useBlock = mapEventDecision(type.getMethod("getUseBlock").invoke(event));
            return new LeftClickBlockResult(cancelled, useItem, useBlock);
        } catch (Throwable ignored) {
            return LeftClickBlockResult.DEFAULT;
        }
    }

    public static RightClickBlockResult onRightClickBlock(ServerPlayer player, InteractionHand hand, BlockPos pos, BlockHitResult hitResult) {
        if (!FORGE_PRESENT) {
            return RightClickBlockResult.DEFAULT;
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method method = hooks.getMethod("onRightClickBlock", Player.class, InteractionHand.class, BlockPos.class, BlockHitResult.class);
            Object event = method.invoke(null, player, hand, pos, hitResult);
            if (event == null) {
                return RightClickBlockResult.DEFAULT;
            }
            Class<?> type = event.getClass();
            boolean cancelled = Boolean.TRUE.equals(type.getMethod("isCanceled").invoke(event));
            Object cancellationResult = type.getMethod("getCancellationResult").invoke(event);
            InteractionResult result = cancellationResult instanceof InteractionResult interactionResult ? interactionResult : InteractionResult.PASS;
            EventDecision useItem = mapEventDecision(type.getMethod("getUseItem").invoke(event));
            EventDecision useBlock = mapEventDecision(type.getMethod("getUseBlock").invoke(event));
            return new RightClickBlockResult(cancelled, result, useItem, useBlock);
        } catch (Throwable ignored) {
            return RightClickBlockResult.DEFAULT;
        }
    }

    public static ShieldBlockResult onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        if (!FORGE_PRESENT) {
            return new ShieldBlockResult(false, amount, true);
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method method = hooks.getMethod("onShieldBlock", LivingEntity.class, DamageSource.class, float.class);
            Object event = method.invoke(null, entity, source, amount);
            if (event == null) {
                return new ShieldBlockResult(false, amount, true);
            }
            Class<?> type = event.getClass();
            boolean cancelled = Boolean.TRUE.equals(type.getMethod("isCanceled").invoke(event));
            float blocked = ((Number) type.getMethod("getBlockedDamage").invoke(event)).floatValue();
            boolean shieldTakesDamage = Boolean.TRUE.equals(type.getMethod("shieldTakesDamage").invoke(event));
            return new ShieldBlockResult(cancelled, blocked, shieldTakesDamage);
        } catch (Throwable ignored) {
            return new ShieldBlockResult(false, amount, true);
        }
    }

    public static LivingChangeTargetResult onLivingChangeTarget(LivingEntity entity, @Nullable LivingEntity newTarget) {
        if (!FORGE_PRESENT) {
            return new LivingChangeTargetResult(false, newTarget);
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Class<?> targetType = Class.forName("net.minecraftforge.event.entity.living.LivingChangeTargetEvent$LivingTargetType");
            Object mobTarget = Enum.valueOf((Class<Enum>) targetType.asSubclass(Enum.class), "MOB_TARGET");
            Method method = hooks.getMethod("onLivingChangeTarget", LivingEntity.class, LivingEntity.class, targetType);
            Object event = method.invoke(null, entity, newTarget, mobTarget);
            if (event == null) {
                return new LivingChangeTargetResult(false, newTarget);
            }
            Class<?> type = event.getClass();
            boolean cancelled = Boolean.TRUE.equals(type.getMethod("isCanceled").invoke(event));
            Object target = type.getMethod("getNewTarget").invoke(event);
            return new LivingChangeTargetResult(cancelled, target instanceof LivingEntity living ? living : newTarget);
        } catch (Throwable ignored) {
            return new LivingChangeTargetResult(false, newTarget);
        }
    }

    public static LivingSwapHandItemsResult onLivingSwapHandItems(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (!FORGE_PRESENT) {
            return new LivingSwapHandItemsResult(false, offHand, mainHand);
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method method = hooks.getMethod("onLivingSwapHandItems", LivingEntity.class);
            Object event = method.invoke(null, player);
            if (event == null) {
                return new LivingSwapHandItemsResult(false, offHand, mainHand);
            }
            Class<?> type = event.getClass();
            boolean cancelled = Boolean.TRUE.equals(type.getMethod("isCanceled").invoke(event));
            Object swappedMain = type.getMethod("getItemSwappedToMainHand").invoke(event);
            Object swappedOff = type.getMethod("getItemSwappedToOffHand").invoke(event);
            ItemStack newMain = swappedMain instanceof ItemStack stack ? stack : offHand;
            ItemStack newOff = swappedOff instanceof ItemStack stack ? stack : mainHand;
            return new LivingSwapHandItemsResult(cancelled, newMain, newOff);
        } catch (Throwable ignored) {
            return new LivingSwapHandItemsResult(false, offHand, mainHand);
        }
    }

    public static CompletableFuture<Component> decorateServerChat(ServerPlayer player, Component decoratedContent) {
        if (!FORGE_PRESENT) {
            return CompletableFuture.completedFuture(decoratedContent);
        }
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks");
            Method getter = hooks.getMethod("getServerChatSubmittedDecorator");
            Object decorator = getter.invoke(null);
            if (decorator == null) {
                return CompletableFuture.completedFuture(decoratedContent);
            }
            Method decorate = decorator.getClass().getMethod("decorate", ServerPlayer.class, Component.class);
            Object result = decorate.invoke(decorator, player, decoratedContent);
            if (result instanceof CompletableFuture<?> future) {
                return future.thenApply(component -> component instanceof Component c ? c : decoratedContent);
            }
        } catch (Throwable ignored) {
        }
        return CompletableFuture.completedFuture(decoratedContent);
    }

    @Nullable
    public static InteractionResult onInteractEntityAt(Player player, Entity entity, Vec3 vec, InteractionHand hand) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onInteractEntityAt",
                new Class<?>[]{Player.class, Entity.class, Vec3.class, InteractionHand.class},
                player,
                entity,
                vec,
                hand
        );
        if (ret instanceof InteractionResult result) {
            return result;
        }
        return null;
    }

    public static void postPlayerContainerOpen(ServerPlayer player, AbstractContainerMenu menu) {
        if (!FORGE_PRESENT) {
            return;
        }
        try {
            Class<?> eventClass = Class.forName("net.minecraftforge.event.entity.player.PlayerContainerEvent$Open");
            Constructor<?> ctor = eventClass.getConstructor(Player.class, AbstractContainerMenu.class);
            Object event = ctor.newInstance(player, menu);
            postForgeEvent(event);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    public static Component getPlayerTabListDisplayName(ServerPlayer player) {
        Object ret = invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "getPlayerTabListDisplayName",
                new Class<?>[]{ServerPlayer.class},
                player
        );
        if (ret instanceof Component component) {
            return component;
        }
        return null;
    }

    public static boolean onPlayerSpawnSet(ServerPlayer player, ResourceKey<Level> dimension, @Nullable BlockPos pos, boolean forced) {
        Object ret = invokeStatic(
                "net.minecraftforge.event.ForgeEventFactory",
                "onPlayerSpawnSet",
                new Class<?>[]{ServerPlayer.class, ResourceKey.class, BlockPos.class, boolean.class},
                player,
                dimension,
                pos,
                forced
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return false;
    }

    public static boolean onItemStackedOn(ItemStack stackOnSlot, ItemStack carriedStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        Object ret = invokeStatic(
                "net.minecraftforge.common.ForgeHooks",
                "onItemStackedOn",
                new Class<?>[]{ItemStack.class, ItemStack.class, Slot.class, ClickAction.class, Player.class, SlotAccess.class},
                stackOnSlot,
                carriedStack,
                slot,
                action,
                player,
                access
        );
        if (ret instanceof Boolean value) {
            return value;
        }
        return false;
    }

    @Nullable
    public static String getMenuTypeTranslationKey(@Nullable MenuType<?> menuType) {
        if (menuType == null) {
            return null;
        }
        var key = BuiltInRegistries.MENU.getKey(menuType);
        return key == null ? null : key.toString();
    }

    public static int getBlockExpDrop(Block block, BlockState state, ServerLevel world, BlockPos pos, ItemStack stack, boolean ignoredDropFlag) {
        int silkTouch = stack.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);
        int fortune = stack.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE);
        if (FORGE_PRESENT) {
            try {
                Method method = block.getClass().getMethod("getExpDrop", BlockState.class, ServerLevel.class, net.minecraft.util.RandomSource.class, BlockPos.class, int.class, int.class);
                Object ret = method.invoke(block, state, world, world.random, pos, fortune, silkTouch);
                if (ret instanceof Integer value) {
                    return value;
                }
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S, T> void mergeCommandNode(
            CommandNode<S> source,
            CommandNode<T> target,
            Map<CommandNode<S>, CommandNode<T>> map,
            S context,
            Command<S> command,
            java.util.function.Function<SuggestionProvider<S>, SuggestionProvider<T>> suggestionMapper
    ) {
        if (!FORGE_PRESENT) {
            return;
        }
        try {
            Class<?> helperClass = Class.forName("net.minecraftforge.server.command.CommandHelper");
            Method method = helperClass.getMethod("mergeCommandNode", CommandNode.class, CommandNode.class, Map.class, Object.class, Command.class, java.util.function.Function.class);
            method.invoke(null, source, target, map, context, command, suggestionMapper);
        } catch (Throwable ignored) {
        }
    }

    private static void postForgeLevelEvent(String eventClassName, ServerLevel level) {
        if (!FORGE_PRESENT) {
            return;
        }
        try {
            Class<?> eventClass = Class.forName(eventClassName);
            Constructor<?> ctor = findSingleArgCtor(eventClass, level.getClass());
            if (ctor == null) {
                return;
            }
            Object event = ctor.newInstance(level);
            postForgeEvent(event);
        } catch (Throwable ignored) {
        }
    }

    private static boolean postForgeEvent(Object event) {
        if (!FORGE_PRESENT) {
            return false;
        }
        try {
            Class<?> minecraftForge = Class.forName("net.minecraftforge.common.MinecraftForge");
            Field busField = minecraftForge.getField("EVENT_BUS");
            Object bus = busField.get(null);
            Method post = bus.getClass().getMethod("post", Class.forName("net.minecraftforge.eventbus.api.Event"));
            Object cancelled = post.invoke(bus, event);
            return cancelled instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    private static Constructor<?> findSingleArgCtor(Class<?> type, Class<?> argType) {
        for (Constructor<?> ctor : type.getConstructors()) {
            if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isAssignableFrom(argType)) {
                return ctor;
            }
        }
        return null;
    }

    @Nullable
    private static Object invokeStatic(String className, String methodName, Class<?>[] paramTypes, Object... args) {
        if (!FORGE_PRESENT) {
            return null;
        }
        try {
            Class<?> type = Class.forName(className);
            Method method = type.getMethod(methodName, paramTypes);
            return method.invoke(null, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static EventDecision mapEventDecision(Object result) {
        if (result instanceof Enum<?> enumValue) {
            return switch (enumValue.name()) {
                case "ALLOW" -> EventDecision.ALLOW;
                case "DENY" -> EventDecision.DENY;
                default -> EventDecision.DEFAULT;
            };
        }
        return EventDecision.DEFAULT;
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, PlatformHooks.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public enum EventDecision {
        DEFAULT,
        ALLOW,
        DENY
    }

    public record ShieldBlockResult(boolean cancelled, float blockedDamage, boolean shieldTakesDamage) {
    }

    public record LivingChangeTargetResult(boolean cancelled, LivingEntity newTarget) {
        public LivingChangeTargetResult(boolean cancelled, @Nullable LivingEntity newTarget) {
            this.cancelled = cancelled;
            this.newTarget = newTarget;
        }

        @Override
        @Nullable
        public LivingEntity newTarget() {
            return newTarget;
        }
    }

    public static final class LivingSwapHandItemsResult {
        private final boolean cancelled;
        private final ItemStack swappedToMainHand;
        private final ItemStack swappedToOffHand;

        public LivingSwapHandItemsResult(boolean cancelled, ItemStack swappedToMainHand, ItemStack swappedToOffHand) {
            this.cancelled = cancelled;
            this.swappedToMainHand = swappedToMainHand;
            this.swappedToOffHand = swappedToOffHand;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public ItemStack getItemSwappedToMainHand() {
            return swappedToMainHand;
        }

        public ItemStack getItemSwappedToOffHand() {
            return swappedToOffHand;
        }
    }

    public static final class LeftClickBlockResult {
        private static final LeftClickBlockResult DEFAULT = new LeftClickBlockResult(false, EventDecision.DEFAULT, EventDecision.DEFAULT);

        private final boolean cancelled;
        private final EventDecision useItem;
        private final EventDecision useBlock;

        public LeftClickBlockResult(boolean cancelled, EventDecision useItem, EventDecision useBlock) {
            this.cancelled = cancelled;
            this.useItem = useItem;
            this.useBlock = useBlock;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isUseItemDenied() {
            return useItem == EventDecision.DENY;
        }

        public boolean isUseBlockDenied() {
            return useBlock == EventDecision.DENY;
        }
    }

    public static final class RightClickBlockResult {
        private static final RightClickBlockResult DEFAULT = new RightClickBlockResult(false, InteractionResult.PASS, EventDecision.DEFAULT, EventDecision.DEFAULT);

        private final boolean cancelled;
        private final InteractionResult cancellationResult;
        private final EventDecision useItem;
        private final EventDecision useBlock;

        public RightClickBlockResult(boolean cancelled, InteractionResult cancellationResult, EventDecision useItem, EventDecision useBlock) {
            this.cancelled = cancelled;
            this.cancellationResult = cancellationResult;
            this.useItem = useItem;
            this.useBlock = useBlock;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public InteractionResult getCancellationResult() {
            return cancellationResult;
        }

        public boolean isUseItemDenied() {
            return useItem == EventDecision.DENY;
        }

        public boolean isUseItemAllowed() {
            return useItem == EventDecision.ALLOW;
        }

        public boolean isUseBlockDenied() {
            return useBlock == EventDecision.DENY;
        }

        public boolean isUseBlockAllowed() {
            return useBlock == EventDecision.ALLOW;
        }
    }
}
