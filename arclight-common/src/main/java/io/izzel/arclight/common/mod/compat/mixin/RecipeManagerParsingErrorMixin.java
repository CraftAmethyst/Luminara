package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeManager.class)
@LoadIfMod(
    modid = ModIds.MODERNFIX,
    condition = LoadIfMod.ModCondition.ABSENT
)
public abstract class RecipeManagerParsingErrorMixin {

    private static final Logger ARCLIGHT_LOGGER =
        ArclightI18nLogger.getLogger("RecipeManager");

    @Redirect(
        method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
        ),
        require = 1
    )
    private void arclight$logParsingError(
        org.slf4j.Logger logger,
        String message,
        Object recipeId,
        Object exception
    ) {
        ARCLIGHT_LOGGER.error(
            "recipe.loading.parsing-error",
            recipeId,
            exception
        );
    }
}
