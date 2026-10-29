package io.izzel.arclight.common.mixin.core.world.entity.monster;

/**
 * Compatibility marker that keeps Arclight's original mixin class name resolvable.
 *
 * <p>Luminara is a fork of Arclight and moved its mixins from {@code io.izzel.arclight.common.mixin}
 * to {@code org.craftamethyst.luminara.common.mixin}. Some mods detect Arclight by probing for this
 * exact class name through Mixin's bytecode provider (for example Supplementaries/Moonlight use
 * {@code @OptionalMixin("io.izzel.arclight.common.mixin.core.world.entity.monster.CreeperMixin")} to
 * decide between a vanilla and an Arclight-specific {@code Creeper} mixin). When the probe fails
 * they fall back to the vanilla mixin, which targets the six-argument {@code Level#explode} call
 * that Luminara's own {@code CreeperMixin} replaces with the seven-argument overload, and the
 * server dies during bootstrap with an {@code InjectionError}.
 *
 * <p>This class is never loaded or referenced by Luminara itself. It only has to exist on the
 * classpath so those compatibility checks can see it.
 */
public final class CreeperMixin {

  private CreeperMixin() {}
}
