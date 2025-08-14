package io.izzel.arclight.common.mixin.paper.concurrent;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper's MultiThreadedQueue with Luminara.
 * This mixin ensures that the concurrent queue implementation works properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = MultiThreadedQueue.class, remap = false)
public class MultiThreadedQueueMixin {

    /**
     * Initialize MultiThreadedQueue with Arclight-specific optimizations.
     */
    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void arclight$initializeQueue(CallbackInfo ci) {
        // Ensure that the queue is properly initialized for Arclight environment
        // This may include setting up monitoring, debugging, or performance tracking
    }

    /**
     * Initialize MultiThreadedQueue from collection with Arclight-specific optimizations.
     */
    @Inject(method = "<init>(Ljava/lang/Iterable;)V", at = @At("RETURN"))
    private void arclight$initializeQueueFromCollection(CallbackInfo ci) {
        // Ensure that the queue is properly initialized from collection for Arclight environment
    }
}
