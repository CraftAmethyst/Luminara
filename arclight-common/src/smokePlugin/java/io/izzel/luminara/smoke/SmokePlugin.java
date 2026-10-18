package io.izzel.luminara.smoke;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
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
        startShutdownHangProbe();
    }

    private void startShutdownHangProbe() {
        CountDownLatch neverReleased = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            try {
                neverReleased.await();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }, "LuminaraSmoke-ShutdownHang");
        thread.setDaemon(false);
        thread.start();
        getLogger().info("LUMINARA_SMOKE_SHUTDOWN_HANG_READY");
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
     * async-catcher mode is BLOCK, so the call must wait for the server thread to replay it.
     * The command returns before this wait begins, leaving the server thread available to
     * perform the replay.
     */
    private void startAsyncGuardProbe() throws Exception {
        Object world = getServer().getWorlds().get(0);
        Thread thread = new Thread(() -> {
            try {
                world.getClass().getMethod("getNMSEntities").invoke(world);
                getLogger().info("LUMINARA_SMOKE_ASYNC_GUARD_OK");
            } catch (Throwable throwable) {
                getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Async guard check failed",
                    throwable
                );
            }
        }, "LuminaraSmoke-AsyncGuard");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        sender.sendMessage("LUMINARA_SMOKE_COMMAND_OK");
        // Start the probe and return immediately so the server thread remains available
        // to replay the guarded call for the BLOCK async-catcher strategy.
        try {
            startAsyncGuardProbe();
        } catch (Exception e) {
            throw new IllegalStateException("Async guard check failed", e);
        }
        return true;
    }
}
