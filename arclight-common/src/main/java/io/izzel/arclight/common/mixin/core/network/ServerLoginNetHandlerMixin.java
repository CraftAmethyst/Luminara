package io.izzel.arclight.common.mixin.core.network;

import static net.minecraft.server.network.ServerLoginPacketListenerImpl.isValidUsername;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.izzel.arclight.common.bridge.core.network.NetworkManagerBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.server.management.PlayerListBridge;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.i18n.ArclightConfig;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginNetHandlerMixin {

    private static final org.apache.logging.log4j.Logger ARCLIGHT_LOGGER =
        ArclightI18nLogger.getLogger("ServerLoginNetHandler");

    // Velocity Modern Forwarding constants/state
    private static final int LUMINARA_VELOCITY_QUERY_ID = 1203961429; // same as PCF
    private static final ResourceLocation LUMINARA_VELOCITY_CHANNEL =
        new ResourceLocation("velocity", "player_info");

    @Shadow
    @Final
    private static AtomicInteger UNIQUE_THREAD_ID;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    public Connection connection;

    private volatile boolean luminara$velocityListen = false;

    // @formatter:off
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @Shadow @Final private MinecraftServer server;

    @Shadow private GameProfile gameProfile;

    @Shadow private ServerPlayer delayedAcceptPlayer;

    @Shadow @Final private byte[] challenge;

    private static boolean arclight$validUsernameCheck(String name) {
        var regex = ArclightConfig.spec().getCompat().getValidUsernameRegex();
        return !regex.isBlank() && name.matches(regex);
    }

    @Shadow protected abstract GameProfile createFakeProfile(GameProfile original);

    // @formatter:on

    @Shadow
    public abstract void disconnect(Component reason);

    @Shadow
    public abstract String getUserName();

    public void disconnect(final String s) {
        this.disconnect(Component.literal(s));
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void handleAcceptedLogin() {
        /*
        if (!this.loginGameProfile.isComplete()) {
            this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
        }
        */

        if (!this.server.usesAuthentication()) {
            // this.gameProfile = this.createFakeProfile(this.gameProfile); // Spigot - Moved to initUUID
            // Spigot end
        }

        ServerPlayer entity =
            ((PlayerListBridge) this.server.getPlayerList()).bridge$canPlayerLogin(
                this.connection.getRemoteAddress(),
                this.gameProfile,
                (ServerLoginPacketListenerImpl) (Object) this
            );
        if (entity == null) {
            // this.disconnect(itextcomponent);
        } else {
            this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
            if (
                this.server.getCompressionThreshold() >= 0 &&
                !this.connection.isMemoryConnection()
            ) {
                this.connection.send(
                    new ClientboundLoginCompressionPacket(
                        this.server.getCompressionThreshold()
                    ),
                    PacketSendListener.thenRun(() -> {
                        this.connection.setupCompression(
                            this.server.getCompressionThreshold(),
                            true
                        );
                    })
                );
            }

            this.connection.send(
                new ClientboundGameProfilePacket(this.gameProfile)
            );
            ServerPlayer serverplayerentity =
                this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            try {
                if (serverplayerentity != null) {
                    this.state =
                        ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                    this.delayedAcceptPlayer = entity;
                } else {
                    this.server.getPlayerList().placeNewPlayer(
                        this.connection,
                        entity
                    );
                }
            } catch (Exception exception) {
                ARCLIGHT_LOGGER.error(
                    "player.place-in-world-failed",
                    exception
                );
                var chatmessage = Component.translatable(
                    "multiplayer.disconnect.invalid_player_data"
                );

                this.connection.send(
                    new ClientboundDisconnectPacket(chatmessage)
                );
                this.connection.disconnect(chatmessage);
            }
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void handleHello(ServerboundHelloPacket packetIn) {
        Validate.validState(
            this.state == ServerLoginPacketListenerImpl.State.HELLO,
            "Unexpected hello packet"
        );
        Validate.validState(
            this.state == ServerLoginPacketListenerImpl.State.HELLO,
            "Unexpected hello packet"
        );
        Validate.validState(
            arclight$validUsernameCheck(packetIn.name()) ||
                isValidUsername(packetIn.name()),
            "Invalid characters in username"
        );

        // Velocity Modern Forwarding: send login query and wait for response (only if backend is offline-mode)
        if (
            ArclightConfig.spec().getVelocity() != null &&
            ArclightConfig.spec().getVelocity().isEnabled() &&
            !this.server.usesAuthentication()
        ) {
            this.luminara$velocityListen = true;
            try {
                this.connection.send(
                    new ClientboundCustomQueryPacket(
                        LUMINARA_VELOCITY_QUERY_ID,
                        LUMINARA_VELOCITY_CHANNEL,
                        new FriendlyByteBuf(Unpooled.EMPTY_BUFFER)
                    )
                );
                ARCLIGHT_LOGGER.info(
                    "velocity.forwarding.enabled-for-player",
                    packetIn.name(),
                    false
                );
            } catch (Throwable t) {
                ARCLIGHT_LOGGER.warn("velocity.query.send-failed", t);
            }
            return;
        }

        GameProfile gameprofile = this.server.getSingleplayerProfile();
        if (
            gameprofile != null &&
            packetIn.name().equalsIgnoreCase(gameprofile.getName())
        ) {
            this.gameProfile = gameprofile;
            this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
        } else {
            this.gameProfile = new GameProfile(null, packetIn.name());
            if (
                this.server.usesAuthentication() &&
                !this.connection.isMemoryConnection()
            ) {
                this.state = ServerLoginPacketListenerImpl.State.KEY;
                this.connection.send(
                    new ClientboundHelloPacket(
                        "",
                        this.server.getKeyPair().getPublic().getEncoded(),
                        this.challenge
                    )
                );
            } else {
                class Handler extends Thread {

                    Handler() {
                        super(
                            SidedThreadGroups.SERVER,
                            "User Authenticator #" +
                                UNIQUE_THREAD_ID.incrementAndGet()
                        );
                    }

                    @Override
                    public void run() {
                        try {
                            initUUID();
                            arclight$preLogin();
                        } catch (Exception ex) {
                            disconnect("Failed to verify username!");
                            ARCLIGHT_LOGGER.warn(
                                "auth.exception-verifying",
                                gameProfile.getName(),
                                ex
                            );
                        }
                    }
                }
                new Handler().start();
            }
        }
    }

    public void initUUID() {
        UUID uuid;
        if (
            ((NetworkManagerBridge) this.connection).bridge$getSpoofedUUID() !=
            null
        ) {
            uuid =
                ((NetworkManagerBridge) this.connection).bridge$getSpoofedUUID();
        } else {
            uuid = UUIDUtil.createOfflinePlayerUUID(this.gameProfile.getName());
        }
        this.gameProfile = new GameProfile(uuid, this.gameProfile.getName());
        if (
            ((NetworkManagerBridge) this.connection).bridge$getSpoofedProfile() !=
            null
        ) {
            Property[] spoofedProfile;
            for (
                int length = (spoofedProfile =
                            ((NetworkManagerBridge) this.connection).bridge$getSpoofedProfile()).length,
                    i = 0;
                i < length;
                ++i
            ) {
                final Property property = spoofedProfile[i];
                this.gameProfile.getProperties().put(
                    property.getName(),
                    property
                );
            }
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void handleKey(ServerboundKeyPacket packetIn) {
        Validate.validState(
            this.state == ServerLoginPacketListenerImpl.State.KEY,
            "Unexpected key packet"
        );

        final String s;
        try {
            PrivateKey privatekey = this.server.getKeyPair().getPrivate();
            if (!packetIn.isChallengeValid(this.challenge, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretKey = packetIn.getSecretKey(privatekey);
            Cipher cipher = Crypt.getCipher(2, secretKey);
            Cipher cipher1 = Crypt.getCipher(1, secretKey);
            s = (new BigInteger(
                    Crypt.digestData(
                        "",
                        this.server.getKeyPair().getPublic(),
                        secretKey
                    )
                )).toString(16);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher1);
        } catch (CryptException cryptexception) {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        class Handler extends Thread {

            Handler(int i) {
                super(SidedThreadGroups.SERVER, "User Authenticator #" + i);
            }

            public void run() {
                GameProfile gameprofile = gameProfile;

                try {
                    gameProfile = server
                        .getSessionService()
                        .hasJoinedServer(
                            new GameProfile(null, gameprofile.getName()),
                            s,
                            this.getAddress()
                        );
                    if (gameProfile != null) {
                        if (!connection.isConnected()) {
                            return;
                        }
                        arclight$preLogin();
                    } else if (server.isSingleplayer()) {
                        ARCLIGHT_LOGGER.warn("auth.verification-failed-allow");
                        gameProfile = createFakeProfile(gameprofile);
                        state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
                    } else {
                        disconnect(
                            Component.translatable(
                                "multiplayer.disconnect.unverified_username"
                            )
                        );
                        ARCLIGHT_LOGGER.error(
                            "auth.invalid-session",
                            gameprofile.getName()
                        );
                    }
                } catch (Exception var3) {
                    if (server.isSingleplayer()) {
                        ARCLIGHT_LOGGER.warn("auth.servers-down-allow");
                        gameProfile = createFakeProfile(gameprofile);
                        state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
                    } else {
                        disconnect(
                            Component.translatable(
                                "multiplayer.disconnect.authservers_down"
                            )
                        );
                        ARCLIGHT_LOGGER.error("auth.servers-unavailable");
                    }
                }
            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketaddress = connection.getRemoteAddress();
                return server.getPreventProxyConnections() &&
                    socketaddress instanceof InetSocketAddress
                    ? ((InetSocketAddress) socketaddress).getAddress()
                    : null;
            }
        }
        Thread thread = new Handler(UNIQUE_THREAD_ID.incrementAndGet());
        thread.setUncaughtExceptionHandler(
            new DefaultUncaughtExceptionHandler(LOGGER)
        );
        thread.start();
    }

    void arclight$preLogin() throws Exception {
        String playerName = gameProfile.getName();
        InetAddress address =
            ((InetSocketAddress) connection.getRemoteAddress()).getAddress();
        UUID uniqueId = gameProfile.getId();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(
            playerName,
            address,
            uniqueId
        );
        craftServer.getPluginManager().callEvent(asyncEvent);
        if (
            PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length !=
            0
        ) {
            PlayerPreLoginEvent event = new PlayerPreLoginEvent(
                playerName,
                address,
                uniqueId
            );
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                event.disallow(
                    asyncEvent.getResult(),
                    asyncEvent.getKickMessage()
                );
            }
            class SyncPreLogin extends Waitable<PlayerPreLoginEvent.Result> {

                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    craftServer.getPluginManager().callEvent(event);
                    return event.getResult();
                }
            }
            Waitable<PlayerPreLoginEvent.Result> waitable = new SyncPreLogin();
            ((MinecraftServerBridge) server).bridge$queuedProcess(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(event.getKickMessage());
                return;
            }
        } else if (
            asyncEvent.getLoginResult() !=
            AsyncPlayerPreLoginEvent.Result.ALLOWED
        ) {
            disconnect(asyncEvent.getKickMessage());
            return;
        }
        if (
            ArclightConfig.spec().getVelocity() != null &&
            ArclightConfig.spec().getVelocity().isEnabled()
        ) {
            ARCLIGHT_LOGGER.info(
                "auth.player-uuid-velocity",
                gameProfile.getName(),
                gameProfile.getId()
            );
        } else {
            ARCLIGHT_LOGGER.info(
                "auth.player-uuid",
                gameProfile.getName(),
                gameProfile.getId()
            );
        }
        state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
    }

    /**
     * Continue the login process after Velocity forwarding (with Mojang authentication)
     */
    private void luminara$continueLogin() {
        // Execute the login process in a separate thread, similar to Mohist's approach
        Thread thread = new Thread("Luminara Velocity Login") {
            @Override
            public void run() {
                try {
                    arclight$preLogin();
                } catch (Exception ex) {
                    disconnect("Failed to verify username!");
                    ARCLIGHT_LOGGER.warn(
                        "auth.exception-verifying-player",
                        gameProfile.getName(),
                        ex
                    );
                }
            }
        };
        thread.setUncaughtExceptionHandler(
            new DefaultUncaughtExceptionHandler(LOGGER)
        );
        thread.start();
    }

    /**
     * Get transaction ID from custom query packet using Mohist's exact method names
     */
    private int luminara$getTransactionId(
        net.minecraft.network.protocol.login.ServerboundCustomQueryPacket packet
    ) throws Exception {
        // Use the exact obfuscated method name from Mohist patch: m_179824_()
        var method = packet.getClass().getMethod("m_179824_");
        return (Integer) method.invoke(packet);
    }

    /**
     * Get data from custom query packet using Mohist's exact method names
     */
    private net.minecraft.network.FriendlyByteBuf luminara$getPacketData(
        net.minecraft.network.protocol.login.ServerboundCustomQueryPacket packet
    ) throws Exception {
        // Use the exact obfuscated method name from Mohist patch: m_179825_()
        var method = packet.getClass().getMethod("m_179825_");
        return (net.minecraft.network.FriendlyByteBuf) method.invoke(packet);
    }

    // Inject: handle Velocity modern forwarding response during login
    @Inject(
        method = "handleCustomQueryPacket",
        at = @At("HEAD"),
        cancellable = true
    )
    private void luminara$onHandleCustomQueryPacket(
        ServerboundCustomQueryPacket packet,
        CallbackInfo ci
    ) {
        if (!this.luminara$velocityListen) return;
        try {
            int id = luminara$getTransactionId(packet);
            if (id != LUMINARA_VELOCITY_QUERY_ID) return;
            this.luminara$velocityListen = false;

            FriendlyByteBuf data = luminara$getPacketData(packet);
            if (data == null) {
                ARCLIGHT_LOGGER.warn("velocity.query.null-response");
                disconnect(
                    Component.literal(
                        "Direct connections to this server are not permitted!"
                    )
                );
                ci.cancel();
                return;
            }

            if (!luminara$validateVelocity(data)) {
                ARCLIGHT_LOGGER.warn("velocity.signature.mismatch");
                disconnect(
                    Component.literal(
                        "Direct connections to this server are not permitted!"
                    )
                );
                ci.cancel();
                return;
            }

            // Parse forwarding payload
            GameProfile forwarded = luminara$readForwardedProfile(data);
            this.gameProfile = forwarded;

            // After Velocity forwarding, backend must not perform client encryption
            luminara$continueLogin();

            ARCLIGHT_LOGGER.info(
                "velocity.forwarding.player-processed-successfully",
                this.gameProfile.getName(),
                false
            );
            ci.cancel();
        } catch (Throwable t) {
            ARCLIGHT_LOGGER.warn(
                "velocity.login.exception-processing",
                this.gameProfile != null
                    ? this.gameProfile.getName()
                    : "unknown",
                t
            );
            disconnect(
                Component.literal(
                    "Direct connections to this server are not permitted!"
                )
            );
            ci.cancel();
        }
    }

    private boolean luminara$validateVelocity(FriendlyByteBuf buffer)
        throws Exception {
        byte[] signature = new byte[32];
        buffer.readBytes(signature);
        byte[] rest = new byte[buffer.readableBytes()];
        int idx = buffer.readerIndex();
        buffer.getBytes(idx, rest);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            String secret = ArclightConfig.spec().getVelocity() != null
                ? ArclightConfig.spec().getVelocity().getForwardingSecret()
                : "";
            mac.init(
                new SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256"
                )
            );
            byte[] computed = mac.doFinal(rest);
            return MessageDigest.isEqual(signature, computed);
        } catch (Exception e) {
            ARCLIGHT_LOGGER.warn("velocity.signature.verification-error", e);
            return false;
        }
    }

    private GameProfile luminara$readForwardedProfile(FriendlyByteBuf data) {
        // version
        int version = data.readVarInt();
        if (version != 1) {
            throw new IllegalStateException(
                "Unsupported Velocity forwarding version: " + version
            );
        }
        // real IP (string)
        String ip = data.readUtf();
        try {
            SocketAddress current = this.connection.getRemoteAddress();
            int port = current instanceof InetSocketAddress
                ? ((InetSocketAddress) current).getPort()
                : 0;
            ((NetworkManagerBridge) this.connection).bridge$setVelocityAddress(
                new InetSocketAddress(ip, port)
            );
        } catch (Throwable t) {
            ARCLIGHT_LOGGER.warn(
                "velocity.address.reflection-failed-trying-bridge",
                t
            );
        }
        // UUID + name
        UUID uuid = data.readUUID();
        String name = data.readUtf(16);
        GameProfile profile = new GameProfile(uuid, name);
        // properties
        int properties = data.readVarInt();
        if (properties > 0) {
            List<Property> props = new ArrayList<>(properties);
            for (int i = 0; i < properties; i++) {
                String propName = data.readUtf();
                String value = data.readUtf();
                boolean hasSig = data.readBoolean();
                String sig = hasSig ? data.readUtf() : "";
                props.add(new Property(propName, value, sig));
            }
            for (Property p : props) {
                profile.getProperties().put(p.getName(), p);
            }
            ((NetworkManagerBridge) this.connection).bridge$setSpoofedProfile(
                props.toArray(new Property[0])
            );
        }
        ((NetworkManagerBridge) this.connection).bridge$setSpoofedUUID(uuid);
        return profile;
    }
}
