package io.izzel.arclight.fabric.mixin.bukkit;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.bukkit.craftbukkit.v.command.VanillaCommandWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;
import java.util.Arrays;

@Mixin(value = VanillaCommandWrapper.class, remap = false)
public abstract class VanillaCommandWrapperMixin_Fabric {

    @Unique
    private static volatile Method luminara$prefixed3;
    @Unique
    private static volatile Method luminara$prefixed2;

    @Unique
    private static int luminara$invokePrefixed(Commands commands, CommandSourceStack source, String parsed, String full) {
        try {
            Method m3 = luminara$prefixed3;
            if (m3 == null) {
                m3 = commands.getClass().getMethod("performPrefixedCommand", CommandSourceStack.class, String.class, String.class);
                m3.setAccessible(true);
                luminara$prefixed3 = m3;
            }
            return (int) m3.invoke(commands, source, parsed, full);
        } catch (NoSuchMethodException ignored) {
            // fall through to 2-arg lookup
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke 3-arg performPrefixedCommand", e);
        }

        try {
            Method m2 = luminara$prefixed2;
            if (m2 == null) {
                m2 = commands.getClass().getMethod("performPrefixedCommand", CommandSourceStack.class, String.class);
                m2.setAccessible(true);
                luminara$prefixed2 = m2;
            }
            return (int) m2.invoke(commands, source, parsed);
        } catch (NoSuchMethodException ignored) {
            // scan for obfuscated variant with same shape
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke 2-arg performPrefixedCommand", e);
        }

        Method fallback = Arrays.stream(commands.getClass().getMethods())
                .filter(it -> it.getReturnType() == int.class)
                .filter(it -> it.getParameterCount() == 2)
                .filter(it -> it.getParameterTypes()[0] == CommandSourceStack.class)
                .filter(it -> it.getParameterTypes()[1] == String.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find compatible command execution method on " + commands.getClass().getName()));

        try {
            fallback.setAccessible(true);
            luminara$prefixed2 = fallback;
            return (int) fallback.invoke(commands, source, parsed);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke fallback command execution method: " + fallback, e);
        }
    }

    @Redirect(
            method = "execute",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    remap = false,
                    target = "Lnet/minecraft/class_2170;performPrefixedCommand(Lnet/minecraft/class_2168;Ljava/lang/String;Ljava/lang/String;)I"
            )
    )
    private int luminara$fabricCompatPrefixed(Commands commands, CommandSourceStack source, String parsed, String full) {
        return luminara$invokePrefixed(commands, source, parsed, full);
    }
}
