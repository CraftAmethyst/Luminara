package io.izzel.arclight.common.mixin.core.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.izzel.arclight.common.bridge.core.inventory.IInventoryBridge;
import io.izzel.arclight.common.bridge.core.item.crafting.RecipeManagerBridge;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements RecipeManagerBridge {

    private static final Logger ARCLIGHT_LOGGER = ArclightI18nLogger.getLogger("RecipeManager");

    // @formatter:off
    @Shadow public Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
    @Shadow private boolean hasErrors;
    @Shadow private Map<ResourceLocation, Recipe<?>> byName;

    @Shadow protected abstract <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> p_44055_);
    // @formatter:on

    @Inject(method = "apply", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void arclight$afterApply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
                                     ProfilerFiller profilerIn, CallbackInfo ci) {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> rebuilt = Maps.newHashMap();
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            Map<ResourceLocation, Recipe<?>> existing = this.recipes.get(type);
            if (existing == null) {
                rebuilt.put(type, new Object2ObjectLinkedOpenHashMap<>());
                continue;
            }
            if (existing instanceof Object2ObjectLinkedOpenHashMap) {
                rebuilt.put(type, existing);
            } else {
                Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> copy = new Object2ObjectLinkedOpenHashMap<>();
                copy.putAll(existing);
                rebuilt.put(type, copy);
            }
        }
        this.recipes = rebuilt;
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(this.byName);
        }
    }

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;"
            ),
            require = 1
    )
    private Recipe<?> arclight$fromJsonForge(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
        Recipe<?> recipe = RecipeManager.fromJson(recipeId, json, context);
        if (recipe == null) {
            ARCLIGHT_LOGGER.info("recipe.loading.skip-null-serializer", recipeId);
        }
        return recipe;
    }

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            ),
            require = 1
    )
    private void arclight$logParsingError(org.slf4j.Logger logger, String message, Object recipeId, Object exception) {
        ARCLIGHT_LOGGER.error("recipe.loading.parsing-error", recipeId, exception);
    }

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
            ),
            require = 1
    )
    private void arclight$logLoadedRecipes(org.slf4j.Logger logger, String message, Object count) {
        ARCLIGHT_LOGGER.info("recipe.loading.completed", count);
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeTypeIn, C inventoryIn, Level worldIn) {
        Optional<T> optional = this.byType(recipeTypeIn).values().stream().filter((recipe) -> {
            return recipe.matches(inventoryIn, worldIn);
        }).findFirst();
        ((IInventoryBridge) inventoryIn).setCurrentRecipe(optional.orElse(null));
        return optional;
    }

    public void addRecipe(Recipe<?> recipe) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        Map<ResourceLocation, Recipe<?>> original = this.recipes.get(recipe.getType());
        Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> map;
        if (!(original instanceof Object2ObjectLinkedOpenHashMap)) {
            Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> hashMap = new Object2ObjectLinkedOpenHashMap<>();
            hashMap.putAll(original);
            this.recipes.put(recipe.getType(), hashMap);
            map = hashMap;
        } else {
            map = ((Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>) original);
        }

        if (this.byName.containsKey(recipe.getId()) || map.containsKey(recipe.getId())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
        } else {
            map.putAndMoveToFirst(recipe.getId(), recipe);
            this.byName.put(recipe.getId(), recipe);
        }
    }

    @Override
    public void bridge$addRecipe(Recipe<?> recipe) {
        addRecipe(recipe);
    }

    public boolean removeRecipe(ResourceLocation mcKey) {
        for (var recipes : recipes.values()) {
            recipes.remove(mcKey);
        }
        return byName.remove(mcKey) != null;
    }

    public void clearRecipes() {
        this.recipes = new HashMap<>();
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            this.recipes.put(type, new Object2ObjectLinkedOpenHashMap<>());
        }
        this.byName = new HashMap<>();
    }

    @Override
    public void bridge$clearRecipes() {
        clearRecipes();
    }
}
