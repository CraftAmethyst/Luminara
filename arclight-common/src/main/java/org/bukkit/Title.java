package org.bukkit;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a title to may be sent to a {@link Player}.
 *
 * <p>A title can be sent without subtitle text.</p>
 */
public record Title(@NotNull BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeIn, int stay,
                    int fadeOut) {

    /**
     * The default number of ticks for the title to fade in.
     */
    public static final int DEFAULT_FADE_IN = 10;
    /**
     * The default number of ticks for the title to stay.
     */
    public static final int DEFAULT_STAY = 70;
    /**
     * The default number of ticks for the title to fade out.
     */
    public static final int DEFAULT_FADE_OUT = 20;

    /**
     * Create a title with the default time values and no subtitle.
     *
     * <p>Times use default values.</p>
     *
     * @param title the main text of the title
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull BaseComponent title) {
        this(title, null);
    }

    /**
     * Create a title with the default time values and no subtitle.
     *
     * <p>Times use default values.</p>
     *
     * @param title the main text of the title
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull BaseComponent[] title) {
        this(title, null);
    }

    /**
     * Create a title with the default time values and no subtitle.
     *
     * <p>Times use default values.</p>
     *
     * @param title the main text of the title
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull String title) {
        this(title, null);
    }

    /**
     * Create a title with the default time values.
     *
     * <p>Times use default values.</p>
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     */
    public Title(@NotNull BaseComponent title, @Nullable BaseComponent subtitle) {
        this(title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    /**
     * Create a title with the default time values.
     *
     * <p>Times use default values.</p>
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     */
    public Title(@NotNull BaseComponent[] title, @Nullable BaseComponent[] subtitle) {
        this(title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    /**
     * Create a title with the default time values.
     *
     * <p>Times use default values.</p>
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     */
    public Title(@NotNull String title, @Nullable String subtitle) {
        this(title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    /**
     * Creates a new title.
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     * @param fadeIn   the number of ticks for the title to fade in
     * @param stay     the number of ticks for the title to stay on screen
     * @param fadeOut  the number of ticks for the title to fade out
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull BaseComponent title, @Nullable BaseComponent subtitle, int fadeIn, int stay, int fadeOut) {
        this(
                new BaseComponent[]{title},
                subtitle == null ? null : new BaseComponent[]{subtitle},
                fadeIn, stay, fadeOut
        );
    }

    /**
     * Creates a new title.
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     * @param fadeIn   the number of ticks for the title to fade in
     * @param stay     the number of ticks for the title to stay on screen
     * @param fadeOut  the number of ticks for the title to fade out
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null) {
            throw new NullPointerException("title");
        }
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    /**
     * Creates a new title.
     *
     * @param title    the main text of the title
     * @param subtitle the secondary text of the title
     * @param fadeIn   the number of ticks for the title to fade in
     * @param stay     the number of ticks for the title to stay on screen
     * @param fadeOut  the number of ticks for the title to fade out
     * @throws NullPointerException if the title is null
     */
    public Title(@NotNull String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null) {
            throw new NullPointerException("title");
        }
        this(new BaseComponent[]{new TextComponent(title)}, subtitle == null ? null : new BaseComponent[]{new TextComponent(subtitle)}, fadeIn, stay, fadeOut);
    }

    /**
     * Gets the text of this title
     *
     * @return the text
     */
    @Override
    @NotNull
    public BaseComponent[] title() {
        return title;
    }

    /**
     * Gets the text of this title's subtitle
     *
     * @return the text
     */
    @Override
    @Nullable
    public BaseComponent[] subtitle() {
        return subtitle;
    }

    /**
     * Gets the number of ticks to fade in.
     *
     * <p>The returned value is not the absolute time, but the number of ticks to fade in (see {@link #DEFAULT_FADE_IN}).</p>
     *
     * @return the number of ticks to fade in
     */
    @Override
    public int fadeIn() {
        return fadeIn;
    }

    /**
     * Gets the number of ticks to stay.
     *
     * <p>The returned value is not the absolute time, but the number of ticks to stay (see {@link #DEFAULT_STAY}).</p>
     *
     * @return the number of ticks to stay
     */
    @Override
    public int stay() {
        return stay;
    }

    /**
     * Gets the number of ticks to fade out.
     *
     * <p>The returned value is not the absolute time, but the number of ticks to fade out (see {@link #DEFAULT_FADE_OUT}).</p>
     *
     * @return the number of ticks to fade out
     */
    @Override
    public int fadeOut() {
        return fadeOut;
    }
}
