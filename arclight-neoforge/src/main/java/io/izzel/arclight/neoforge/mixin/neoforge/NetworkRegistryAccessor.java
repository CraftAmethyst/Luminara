package io.izzel.arclight.neoforge.mixin.neoforge;

import java.util.Map;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NetworkRegistry.class, remap = false)
public interface NetworkRegistryAccessor {
  @Accessor("PAYLOAD_REGISTRATIONS")
  static Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> getRegistration() {
    throw new AbstractMethodError();
  }

  @Accessor("BUILTIN_PAYLOADS")
  static Map<ResourceLocation, StreamCodec<?, ?>> getBuiltinPayload() {
    throw new AbstractMethodError();
  }
}
