package io.izzel.arclight.neoforge.mixin.core.world.level.levelgen.structure.templatesystem;

import io.izzel.arclight.common.bridge.core.world.level.levelgen.structure.templatesystem.StructureTemplateBridge;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin_NeoForge implements StructureTemplateBridge {}
