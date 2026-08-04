package io.izzel.arclight.common.adventure;

import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Simple SignedMessage implementation
public class SimpleSignedMessage implements SignedMessage {

    private final String message;
    private final boolean signed;
    private final Instant timestamp;
    private final UUID sender;

    public SimpleSignedMessage(String message, boolean signed, UUID sender) {
        this.message = message;
        this.signed = signed;
        this.sender = sender;
        this.timestamp = Instant.now();
    }

    public static SignedMessage unsigned(String message) {
        return new SimpleSignedMessage(message, false, null);
    }

    public static SignedMessage signed(String message, UUID sender) {
        return new SimpleSignedMessage(message, true, sender);
    }

    @Override
    public @NotNull String message() {
        return message;
    }

    @Override
    public @NotNull Component unsignedContent() {
        return Component.text(message);
    }

    @Override
    public @NotNull Instant timestamp() {
        return timestamp;
    }

    @Override
    public long salt() {
        return 0L;
    }

    @Override
    public @Nullable Signature signature() {
        return null;
    }

    @Override
    public boolean canDelete() {
        return !signed;
    }

    public @NotNull SignedMessage withUnsignedContent(
        @NotNull Component unsignedContent
    ) {
        return new SimpleSignedMessage(
            PaperAdventure.asPlain(unsignedContent),
            this.signed,
            this.sender
        );
    }

    @Override
    public @NotNull Identity identity() {
        return sender != null ? Identity.identity(sender) : Identity.nil();
    }
}
