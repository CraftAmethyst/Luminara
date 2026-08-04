package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.network.chat.ComponentBridgeHandler;
import java.util.Iterator;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftChatMessage.class, remap = false)
public class CraftChatMessageMixin {

    @Redirect(
        method = "fromComponent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;iterator()Ljava/util/Iterator;"
        )
    )
    private static Iterator<Component> arclight$useComponentBridge(
        Component component
    ) {
        return ComponentBridgeHandler.createIterator(component);
    }
}
