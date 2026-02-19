package io.izzel.arclight.common.mixin.core.world.entity.monster;

import net.minecraft.world.entity.monster.SpellcasterIllager;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpellcasterIllager.SpellcasterUseSpellGoal.class)
public abstract class SpellcastingIllager_UseSpellGoalMixin {

    // @formatter:off
    @Shadow(aliases = {"this$0", "f_33776_", "field_7386"}, remap = false) private SpellcasterIllager field_7386;
    @Shadow(aliases = {"m_7269_", "method_7147"}, remap = false) protected abstract SpellcasterIllager.IllagerSpell getSpell();
    // @formatter:on

    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/SpellcasterIllager$SpellcasterUseSpellGoal;performSpellCasting()V"))
    private void arclight$castSpell(CallbackInfo ci) {
        if (!CraftEventFactory.handleEntitySpellCastEvent(this.field_7386, this.getSpell())) {
            ci.cancel();
        }
    }
}
