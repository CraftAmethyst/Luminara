/*
 * Copyright (c) 2017 Daniel Ennis (Aikar) MIT License
 */

package com.destroystokyo.paper.event.server;

import com.google.common.base.Preconditions;
import io.papermc.paper.util.TransformingRandomAccessList;
import net.kyori.adventure.text.Component;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AsyncTabCompleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final CommandSender sender;

    @NotNull
    private final String buffer;

    private final boolean isCommand;

    @Nullable
    private final Location loc;

    private final List<Completion> completions = new ArrayList<>();
    private final List<String> stringCompletions =
        new TransformingRandomAccessList<>(
            this.completions,
            Completion::suggestion,
            Completion::completion
        );
    private boolean cancelled;
    private boolean handled;
    private boolean fireSyncHandler = true;

    public AsyncTabCompleteEvent(
        @NotNull CommandSender sender,
        @NotNull String buffer,
        boolean isCommand,
        @Nullable Location loc
    ) {
        super(true);
        this.sender = sender;
        this.buffer = buffer;
        this.isCommand = isCommand;
        this.loc = loc;
    }

    @Deprecated
    public AsyncTabCompleteEvent(
        @NotNull CommandSender sender,
        @NotNull List<String> completions,
        @NotNull String buffer,
        boolean isCommand,
        @Nullable Location loc
    ) {
        super(true);
        this.sender = sender;
        this.completions.addAll(fromStrings(completions));
        this.buffer = buffer;
        this.isCommand = isCommand;
        this.loc = loc;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static @NotNull List<Completion> fromStrings(
        final @NotNull List<String> strings
    ) {
        final List<Completion> list = new ArrayList<>();
        for (final String it : strings) {
            list.add(new CompletionImpl(it, null));
        }
        return list;
    }

    @NotNull
    public CommandSender getSender() {
        return sender;
    }

    @NotNull
    public List<String> getCompletions() {
        return this.stringCompletions;
    }

    public void setCompletions(@NotNull List<String> completions) {
        if (completions == this.stringCompletions) {
            return;
        }
        Preconditions.checkNotNull(completions, "completions");
        this.completions.clear();
        this.completions.addAll(fromStrings(completions));
    }

    @NotNull
    public List<Completion> completions() {
        return this.completions;
    }

    public void completions(@NotNull List<Completion> newCompletions) {
        Preconditions.checkNotNull(newCompletions, "new completions");
        this.completions.clear();
        this.completions.addAll(newCompletions);
    }

    @NotNull
    public String getBuffer() {
        return buffer;
    }

    public boolean isCommand() {
        return isCommand;
    }

    @Nullable
    public Location getLocation() {
        return loc;
    }

    public boolean isHandled() {
        return !completions.isEmpty() || handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public boolean isFireSyncHandler() {
        return fireSyncHandler;
    }

    public void setFireSyncHandler(boolean fireSyncHandler) {
        this.fireSyncHandler = fireSyncHandler;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public interface Completion extends Examinable {
        static @NotNull Completion completion(
            final @NotNull String suggestion
        ) {
            return new CompletionImpl(suggestion, null);
        }

        static @NotNull Completion completion(
            final @NotNull String suggestion,
            final @Nullable Component tooltip
        ) {
            return new CompletionImpl(suggestion, tooltip);
        }

        @NotNull
        String suggestion();

        @Nullable
        Component tooltip();

        @Override
        default @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
            return Stream.of(
                ExaminableProperty.of("suggestion", this.suggestion()),
                ExaminableProperty.of("tooltip", this.tooltip())
            );
        }
    }

    record CompletionImpl(
        String suggestion,
        Component tooltip
    ) implements Completion {
        CompletionImpl(
            final @NotNull String suggestion,
            final @Nullable Component tooltip
        ) {
            this.suggestion = suggestion;
            this.tooltip = tooltip;
        }

        @Override
        public @NotNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @Nullable Component tooltip() {
            return this.tooltip;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final CompletionImpl that = (CompletionImpl) o;
            return (
                this.suggestion.equals(that.suggestion) &&
                    Objects.equals(this.tooltip, that.tooltip)
            );
        }

        @Override
        public @NotNull String toString() {
            return StringExaminer.simpleEscaping().examine(this);
        }
    }
}
