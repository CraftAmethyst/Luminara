package io.izzel.arclight.common.bridge.network.chat;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentBridgeHandlerTest {

    @Test
    void iteratesComponentsDepthFirst() {
        var root = Component.literal("root");
        var first = Component.literal("first");
        var nested = Component.literal("nested");
        var second = Component.literal("second");
        first.append(nested);
        root.append(first);
        root.append(second);

        List<Component> components = ComponentBridgeHandler
            .createStream(root)
            .toList();

        assertEquals(List.of(root, first, nested, second), components);
    }
}
