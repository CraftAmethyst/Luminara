package io.izzel.arclight.common.bridge.core.entity.monster.piglin;

import java.util.Set;
import net.minecraft.world.item.Item;

public interface PiglinBridge {
    Set<Item> bridge$getAllowedBarterItems();

    Set<Item> bridge$getInterestItems();
}
