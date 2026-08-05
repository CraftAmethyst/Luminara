package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Iterate pending chunk updates with protection against concurrent modification exceptions.
 *
 * <p>This redirect targets the same {@code Set.forEach(Consumer)} call site in
 * {@code DistanceManager.runAllUpdates} as FastChunkGen's
 * {@code MixinChunkTicketManager.replaceIterationForHolderTicking}; both provide equivalent
 * CME protection (FastChunkGen iterates over a {@code toArray()} copy). A Mixin @Redirect is
 * exclusive per call site: FastChunkGen's mixin uses the default priority (1000) while this
 * config uses {@code mixinPriority: 500}, so FastChunkGen is applied first and this redirect
 * would fail its injection check and crash the server with a MixinTransformerError. Therefore
 * this mixin is skipped while FastChunkGen is present.
 */
@Mixin(DistanceManager.class)
@LoadIfMod(
    modid = ModIds.FASTCHUNKGEN,
    condition = LoadIfMod.ModCondition.ABSENT
)
public class DistanceManagerSafeIterMixin {

    @Redirect(
        method = "runAllUpdates",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V"
        )
    )
    private void arclight$safeIter(
        Set<ChunkHolder> instance,
        Consumer<ChunkHolder> consumer
    ) {
        // Iterate pending chunk updates with protection against concurrent modification exceptions
        var iter = instance.iterator();
        var expectedSize = instance.size();
        do {
            var chunkHolder = iter.next();
            iter.remove();
            expectedSize--;

            consumer.accept(chunkHolder);

            // Reset iterator if set was modified using add()
            if (instance.size() != expectedSize) {
                expectedSize = instance.size();
                iter = instance.iterator();
            }
        } while (iter.hasNext());
    }
}
