package io.izzel.arclight.common.bridge.network.chat;

import io.izzel.arclight.common.bridge.core.util.text.ITextComponentBridge;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ComponentBridgeHandler {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");
    private static final Logger ARCLIGHT_LOGGER = ArclightI18nLogger.getLogger(
        "ComponentBridge"
    );
    private static final ConcurrentMap<Class<?>, Method> METHOD_CACHE =
        new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    // Initialize the bridge handler after all classes are loaded
    public static void initialize() {
        if (initialized) return;

        try {
            Class<?> componentClass = Component.class;

            // Try to find getSiblings method with different possible names
            Method getSiblingsMethod = null;
            String[] possibleNames = {
                "getSiblings",
                "m_7220_",
                "m_130940_",
                "siblings",
            };

            for (String methodName : possibleNames) {
                try {
                    getSiblingsMethod = componentClass.getDeclaredMethod(
                        methodName
                    );
                    getSiblingsMethod.setAccessible(true);
                    break;
                } catch (NoSuchMethodException ignored) {
                    // Try next name
                }
            }

            // If still not found, try to find method by return type
            if (getSiblingsMethod == null) {
                for (Method method : componentClass.getDeclaredMethods()) {
                    // Only accept the abstract sibling accessor, never default helper methods (e.g. toFlatList)
                    if (
                        method.getReturnType().equals(List.class) &&
                        method.getParameterCount() == 0 &&
                        Modifier.isAbstract(method.getModifiers()) &&
                        returnsComponentList(method)
                    ) {
                        getSiblingsMethod = method;
                        getSiblingsMethod.setAccessible(true);
                        break;
                    }
                }
            }

            if (getSiblingsMethod != null) {
                METHOD_CACHE.put(componentClass, getSiblingsMethod);
                LOGGER.debug(
                    "ComponentBridgeHandler initialized successfully with method: " +
                        getSiblingsMethod.getName()
                );
            } else {
                ARCLIGHT_LOGGER.error("component.bridge.method-not-found");
            }
        } catch (Exception e) {
            ARCLIGHT_LOGGER.error(
                "component.bridge.init-failed",
                e.getMessage()
            );
            e.printStackTrace();
        } finally {
            // Always mark as initialized to prevent infinite retry loops
            initialized = true;
        }
    }

    // Get siblings from a Component using cached reflection
    @SuppressWarnings("unchecked")
    public static List<Component> getSiblings(Component component) {
        if (component == null) {
            return List.of();
        }

        if (!initialized) {
            initialize();
        }

        try {
            Method method = METHOD_CACHE.get(Component.class);
            if (method != null) {
                Object result = method.invoke(component);
                if (result instanceof List) {
                    return (List<Component>) result;
                }
            }
        } catch (Exception e) {
            ARCLIGHT_LOGGER.error(
                "component.bridge.get-siblings-failed",
                e.getMessage()
            );
        }

        // Fallback to empty list
        return List.of();
    }

    private static boolean returnsComponentList(Method method) {
        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType pType)) {
            return false;
        }
        Type[] args = pType.getActualTypeArguments();
        return (
            args.length == 1 &&
            args[0]
                .getTypeName()
                .contains("net.minecraft.network.chat.Component")
        );
    }

    // Create a stream of components (replaces ComponentMixin.stream())
    public static Stream<Component> createStream(Component component) {
        if (component == null) {
            return Stream.empty();
        }

        if (!initialized) {
            initialize();
        }

        try {
            Set<Component> visited = Collections.newSetFromMap(
                new IdentityHashMap<>()
            );
            List<Component> flattened = new java.util.ArrayList<>();
            ArrayDeque<Component> queue = new ArrayDeque<>();
            queue.add(component);
            while (!queue.isEmpty()) {
                Component current = queue.pollFirst();
                if (current == null || !visited.add(current)) {
                    continue;
                }
                flattened.add(current);
                List<Component> siblings = getSiblings(current);
                if (siblings != null && !siblings.isEmpty()) {
                    queue.addAll(siblings);
                }
            }
            return flattened.stream();
        } catch (Exception e) {
            ARCLIGHT_LOGGER.error(
                "component.bridge.create-stream-failed",
                e.getMessage()
            );
            return Stream.of(component);
        }
    }

    // Create an iterator for components (replaces ComponentMixin.iterator())
    public static Iterator<Component> createIterator(Component component) {
        if (component == null) {
            return Collections.emptyIterator();
        }

        if (!initialized) {
            initialize();
        }

        try {
            return createStream(component).iterator();
        } catch (Exception e) {
            ARCLIGHT_LOGGER.error(
                "component.bridge.create-iterator-failed",
                e.getMessage()
            );
            return List.of(component).iterator();
        }
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
