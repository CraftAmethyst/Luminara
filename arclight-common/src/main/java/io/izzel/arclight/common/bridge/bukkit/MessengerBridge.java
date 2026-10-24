package io.izzel.arclight.common.bridge.bukkit;

import io.izzel.arclight.common.mod.plugin.messaging.ArclightPluginChannel;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

public interface MessengerBridge {
  Object2BooleanOpenHashMap<String> valid = new Object2BooleanOpenHashMap<>();

  ArclightPluginChannel<?> arclight$setupChannel(
      ResourceLocation channel,
      Set<PluginMessageListenerRegistration> incoming,
      Set<Plugin> outgoing);

  void arclight$sendCustomPayload(
      Plugin src, CraftPlayer dst, ResourceLocation location, byte[] data);

  void arclight$registerAnonymousOutgoing(ResourceLocation location);

  ArclightPluginChannel<?> arclight$getAndCheckCrossSend(Plugin src, ResourceLocation channel);

  void arclight$checkUnsafeSend(Plugin src, ResourceLocation channel);
}
