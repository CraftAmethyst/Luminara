package io.izzel.arclight.common.bridge.bukkit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public interface ItemMetaBridge {
    CompoundTag bridge$getForgeCaps();

    void bridge$setForgeCaps(CompoundTag nbt);

    void bridge$offerUnhandledTags(CompoundTag nbt);

    Map<String, Tag> bridge$getUnhandledTags();

    void bridge$setUnhandledTags(Map<String, Tag> tags);
}
