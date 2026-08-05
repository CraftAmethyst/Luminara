package io.izzel.arclight.common.adventure.providers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

// Enhanced MiniMessage provider implementation
@SuppressWarnings("UnstableApiUsage") // permitted provider
public class MiniMessageProviderImpl implements MiniMessage.Provider {

    private static final MiniMessage INSTANCE = createMiniMessage();

    private static MiniMessage createMiniMessage() {
        return MiniMessage.builder()
            .tags(
                TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.reset(),
                    StandardTags.font(),
                    StandardTags.newline(),
                    StandardTags.selector(),
                    StandardTags.insertion(),
                    StandardTags.keybind(),
                    StandardTags.translatable(),
                    StandardTags.transition()
                )
            )
            .build();
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return INSTANCE;
    }

    @Override
    public @NotNull Consumer<MiniMessage.Builder> builder() {
        return builder ->
            builder.tags(
                TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.reset(),
                    StandardTags.font(),
                    StandardTags.newline(),
                    StandardTags.selector(),
                    StandardTags.insertion(),
                    StandardTags.keybind(),
                    StandardTags.translatable(),
                    StandardTags.transition()
                )
            );
    }
}
