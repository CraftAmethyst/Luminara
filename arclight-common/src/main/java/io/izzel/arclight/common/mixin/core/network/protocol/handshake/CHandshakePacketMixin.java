package io.izzel.arclight.common.mixin.core.network.protocol.handshake;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkHooks;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientIntentionPacket.class)
public class CHandshakePacketMixin {

    private static final String EXTRA_DATA = "extraData";
    private static final Gson GSON = new Gson();

    @Shadow
    public String hostName;

    private transient String arclight$host;

    @Redirect(
        method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"
        )
    )
    private String arclight$bungeeHostname(
        FriendlyByteBuf packetBuffer,
        int maxLength
    ) {
        try {
            if (packetBuffer.readableBytes() < 1) {
                return "";
            }

            // Read string length first to validate
            int readerIndex = packetBuffer.readerIndex();
            int stringLength = packetBuffer.readVarInt();

            // Check if we have enough bytes for the string
            if (
                stringLength < 0 || stringLength > packetBuffer.readableBytes()
            ) {
                packetBuffer.readerIndex(readerIndex);
                return "";
            }

            // Reset reader index and read normally
            packetBuffer.readerIndex(readerIndex);
            return packetBuffer.readUtf(Short.MAX_VALUE);
        } catch (Exception e) {
            System.err.println(
                "[Luminara] Error reading hostname from handshake packet: " +
                    e.getMessage()
            );
            return "";
        }
    }

    @Redirect(
        method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraftforge/network/NetworkHooks;getFMLVersion(Ljava/lang/String;)Ljava/lang/String;"
        )
    )
    private String arclight$readFromProfile(String ip) {
        try {
            String fmlVersion = NetworkHooks.getFMLVersion(ip);
            if (
                SpigotConfig.bungee &&
                !Objects.equals(fmlVersion, NetworkConstants.NETVERSION)
            ) {
                if (ip == null || ip.isEmpty()) {
                    return fmlVersion;
                }

                String[] split = ip.split("\0");
                if (
                    split.length == 4 && split[3] != null && !split[3].isEmpty()
                ) {
                    try {
                        Property[] properties = GSON.fromJson(
                            split[3],
                            Property[].class
                        );
                        if (properties != null) {
                            for (Property property : properties) {
                                if (
                                    property != null &&
                                    Objects.equals(
                                        property.getName(),
                                        EXTRA_DATA
                                    )
                                ) {
                                    String value = property.getValue();
                                    if (value != null) {
                                        String extraData = value.replace(
                                            "\1",
                                            "\0"
                                        );
                                        this.arclight$host = ip;
                                        return NetworkHooks.getFMLVersion(
                                            split[0] + extraData
                                        );
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(
                            "[Luminara] Error parsing BungeeCord profile data: " +
                                e.getMessage()
                        );
                    }
                }
            }
            return fmlVersion;
        } catch (Exception e) {
            System.err.println(
                "[Luminara] Error processing FML version: " + e.getMessage()
            );
            return NetworkConstants.NETVERSION;
        }
    }

    @Inject(
        method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
        at = @At("RETURN")
    )
    private void arclight$writeBack(
        FriendlyByteBuf p_179801_,
        CallbackInfo ci
    ) {
        if (arclight$host != null) {
            this.hostName = arclight$host;
            arclight$host = null;
        }
    }
}
