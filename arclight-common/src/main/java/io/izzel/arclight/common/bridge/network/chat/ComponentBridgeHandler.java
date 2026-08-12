package io.izzel.arclight.common.bridge.network.chat;

import io.izzel.arclight.common.bridge.core.util.text.ITextComponentBridge;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Stream;

public class ComponentBridgeHandler {

    public static List<Component> getSiblings(Component component) {
        return component == null ? List.of() : component.getSiblings();
    }

    public static Stream<Component> createStream(Component component) {
        if (component == null) {
            return Stream.empty();
        }

        Set<Component> visited = Collections.newSetFromMap(
            new IdentityHashMap<>()
        );
        List<Component> flattened = new ArrayList<>();
        ArrayDeque<Component> stack = new ArrayDeque<>();
        stack.push(component);
        while (!stack.isEmpty()) {
            Component current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            flattened.add(current);
            List<Component> siblings = current.getSiblings();
            for (int i = siblings.size() - 1; i >= 0; i--) {
                Component sibling = siblings.get(i);
                if (sibling != null) {
                    stack.push(sibling);
                }
            }
        }
        return flattened.stream();
    }

    public static Iterator<Component> createIterator(Component component) {
        return createStream(component).iterator();
    }

    // Bridge method to handle Component iteration
    public static Iterable<Component> asIterable(Component component) {
        return () -> createIterator(component);
    }

    // Create a bridge instance for a component
    public static ITextComponentBridge createBridge(Component component) {
        return new ComponentBridge(component);
    }

    // Implementation of ITextComponentBridge functionality
    public static class ComponentBridge implements ITextComponentBridge {

        private final Component component;

        public ComponentBridge(Component component) {
            this.component = component;
        }

        @Override
        public Stream<Component> bridge$stream() {
            return createStream(component);
        }

        @Override
        public Iterator<Component> bridge$iterator() {
            return createIterator(component);
        }
    }
}
