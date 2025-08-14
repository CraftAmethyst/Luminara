package io.izzel.arclight.common.mixin.paper.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * MC Dev fixes for Util class from Paper patch 0006.
 * This mixin fixes decompilation issues in the Util class.
 */
@Mixin(Util.class)
public class UtilMixin_MCDevFixes {

    /**
     * Fix decompilation issue in identityStrategy method.
     * Paper patch 0006 adds explicit cast to fix generic type issues.
     *
     * @author Paper
     * @reason Fix decompile issue with generic types
     */
    @Overwrite
    public static <K> Hash.Strategy<K> identityStrategy() {
        // Paper - decompile fix: Use reflection to access IdentityStrategy
        try {
            Class<?> identityStrategyClass = Class.forName("net.minecraft.Util$IdentityStrategy");
            java.lang.reflect.Field instanceField = identityStrategyClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            return (Hash.Strategy<K>) instanceField.get(null);
        } catch (Exception e) {
            // Fallback to a simple identity strategy
            return new Hash.Strategy<K>() {
                @Override
                public int hashCode(K o) {
                    return System.identityHashCode(o);
                }

                @Override
                public boolean equals(K a, K b) {
                    return a == b;
                }
            };
        }
    }
}
