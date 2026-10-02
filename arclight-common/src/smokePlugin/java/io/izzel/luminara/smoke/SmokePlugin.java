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

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        sender.sendMessage("LUMINARA_SMOKE_COMMAND_OK");
        return true;
    }
}
