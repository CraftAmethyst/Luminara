package io.izzel.luminara.smoke;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public final class SmokePlugin extends JavaPlugin {

    /**
     * Fails when ENUM$VALUES is still final. Arclight replaces that array while extending
     * the enum, and a still-final field lets the JIT keep returning the original array,
     * so values() would silently miss every value Luminara adds.
     */
    private static void assertDefinalized(Class<?> type) throws Exception {
        Field values = type.getDeclaredField("ENUM$VALUES");
        if (Modifier.isFinal(values.getModifiers())) {
            throw new IllegalStateException(
                type.getName() + ".ENUM$VALUES is still final"
            );
        }
        System.out.println(
            "LUMINARA_SMOKE_ENUM_DEFINALIZED " +
                type.getSimpleName() +
                "=" +
                values.getType().getComponentType().getSimpleName()
        );
    }

    @Override
    public void onEnable() {
        try {
            assertDefinalized(Material.class);
            assertDefinalized(SpawnCategory.class);
            assertExtendedBukkitApi();
        } catch (Exception e) {
            throw new IllegalStateException("Enum definalization check failed", e);
        }
        getLogger().info("LUMINARA_SMOKE_PLUGIN_ENABLED");
        Logger.getLogger("LuminaraSmokeJul").info("LUMINARA_SMOKE_JUL_BRIDGE");
        org.bukkit.Bukkit.getLogger().info(
            "LUMINARA_SMOKE_SERVER_LOGGER_BRIDGE"
        );
        getServer().getConsoleSender().sendMessage(
            "§x§1§2§3§4§5§6LUMINARA_SMOKE_HEX_COLOR"
        );
        getServer().getConsoleSender().sendMessage(
            "§xLUMINARA_SMOKE_HEX_MALFORMED"
        );
        getServer().getConsoleSender().sendMessage(
            "&x&1&2&3&4&5&6LUMINARA_SMOKE_AMP_HEX_COLOR"
        );
        getServer().getConsoleSender().sendMessage(
            "&aLUMINARA_SMOKE_AMP_LEGACY_COLOR"
        );
    }

    private static void assertExtendedBukkitApi() throws Exception {
        Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
        Method minecraftVersion = bukkit.getMethod("getMinecraftVersion");
        Method commandMap = bukkit.getMethod("getCommandMap");
        if (minecraftVersion.invoke(null) == null || commandMap.invoke(null) == null) {
            throw new IllegalStateException("Extended Bukkit API returned null");
        }
        System.out.println("LUMINARA_SMOKE_BUKKIT_API_OK");
    }

    /**
     * Calls a guarded world method from a non-server thread. Luminara's default
     * async-catcher mode is BLOCK, so the guard has to refuse or replay instead of letting
     * the call touch world state concurrently; either way the call must not return
     * normally from this thread, because that would mean the guard is not installed.
     */
    private void assertAsyncGuard() throws Exception {
        Object world = getServer().getWorlds().get(0);
        java.util.concurrent.atomic.AtomicReference<Throwable> outcome =
            new java.util.concurrent.atomic.AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                world.getClass().getMethod("getNMSEntities").invoke(world);
            } catch (Throwable throwable) {
                outcome.set(throwable);
            }
        }, "LuminaraSmoke-AsyncGuard");
        thread.start();
        // The command returns while this probe thread is still blocking, so the server
        // thread is free to replay the guarded call for BLOCK instead of deadlocking on it.
        thread.join(10000);
        if (thread.isAlive()) {
            thread.interrupt();
            throw new IllegalStateException(
                "Async guard blocked the probe thread; the catcher did not return"
            );
        }
        Throwable thrown = outcome.get();
        if (thrown != null) {
            throw new IllegalStateException(
                "Guarded world access from a plugin thread failed instead of being " +
                    "replayed on the server thread: " + thrown,
                thrown
            );
        }
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        sender.sendMessage("LUMINARA_SMOKE_COMMAND_OK");
        // The probe runs here rather than in onEnable: onEnable already runs on the server
        // thread, and waiting there would deadlock the BLOCK strategy, which replays the
        // guarded call on that same thread.
        try {
            assertAsyncGuard();
            sender.sendMessage("LUMINARA_SMOKE_ASYNC_GUARD_OK");
        } catch (Exception e) {
            throw new IllegalStateException("Async guard check failed", e);
        }
        return true;
    }
}
