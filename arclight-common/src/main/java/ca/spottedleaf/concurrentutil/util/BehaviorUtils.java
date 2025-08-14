package ca.spottedleaf.concurrentutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Map;
import java.util.Optional;

/**
 * Behavior utility class for Paper's AI system enhancements.
 * This class provides utilities for entity AI behavior management.
 * <p>
 * This implementation is from Paper patch 0009 (Minecraft Utils).
 */
public final class BehaviorUtils {

    private BehaviorUtils() {
    }

    /**
     * Checks if an entity can reach a target position.
     *
     * @param entity      The entity
     * @param target      The target position
     * @param maxDistance The maximum distance to check
     * @return true if the entity can reach the target
     */
    public static boolean canReach(final Mob entity, final BlockPos target, final int maxDistance) {
        if (entity == null || target == null) {
            return false;
        }

        final double distanceSquared = entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (distanceSquared > maxDistance * maxDistance) {
            return false;
        }

        // Check if there's a valid path
        final Path path = entity.getNavigation().createPath(target, 0);
        return path != null && path.canReach();
    }

    /**
     * Checks if an entity can see another entity.
     *
     * @param viewer The viewing entity
     * @param target The target entity
     * @return true if the viewer can see the target
     */
    public static boolean canSee(final LivingEntity viewer, final Entity target) {
        if (viewer == null || target == null) {
            return false;
        }

        return viewer.hasLineOfSight(target);
    }

    /**
     * Gets the distance between two entities.
     *
     * @param entity1 The first entity
     * @param entity2 The second entity
     * @return The distance between the entities
     */
    public static double getDistance(final Entity entity1, final Entity entity2) {
        if (entity1 == null || entity2 == null) {
            return Double.MAX_VALUE;
        }

        return entity1.distanceTo(entity2);
    }

    /**
     * Gets the distance squared between two entities.
     *
     * @param entity1 The first entity
     * @param entity2 The second entity
     * @return The distance squared between the entities
     */
    public static double getDistanceSquared(final Entity entity1, final Entity entity2) {
        if (entity1 == null || entity2 == null) {
            return Double.MAX_VALUE;
        }

        return entity1.distanceToSqr(entity2);
    }

    /**
     * Checks if an entity is within range of a position.
     *
     * @param entity The entity
     * @param pos    The position
     * @param range  The range to check
     * @return true if the entity is within range
     */
    public static boolean isWithinRange(final Entity entity, final BlockPos pos, final double range) {
        if (entity == null || pos == null) {
            return false;
        }

        return entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= range * range;
    }

    /**
     * Checks if an entity is within range of another entity.
     *
     * @param entity1 The first entity
     * @param entity2 The second entity
     * @param range   The range to check
     * @return true if the entities are within range
     */
    public static boolean isWithinRange(final Entity entity1, final Entity entity2, final double range) {
        if (entity1 == null || entity2 == null) {
            return false;
        }

        return entity1.distanceToSqr(entity2) <= range * range;
    }

    /**
     * Sets a memory value for an entity if it has a brain.
     *
     * @param entity     The entity
     * @param memoryType The memory type
     * @param value      The value to set
     * @param <T>        The type of the memory value
     */
    public static <T> void setMemory(final LivingEntity entity, final MemoryModuleType<T> memoryType, final T value) {
        if (entity == null || memoryType == null || !entity.getBrain().hasMemoryValue(memoryType)) {
            return;
        }

        entity.getBrain().setMemory(memoryType, value);
    }

    /**
     * Gets a memory value from an entity's brain.
     *
     * @param entity     The entity
     * @param memoryType The memory type
     * @param <T>        The type of the memory value
     * @return The memory value, or empty if not present
     */
    public static <T> Optional<T> getMemory(final LivingEntity entity, final MemoryModuleType<T> memoryType) {
        if (entity == null || memoryType == null) {
            return Optional.empty();
        }

        return entity.getBrain().getMemory(memoryType);
    }

    /**
     * Erases a memory from an entity's brain.
     *
     * @param entity     The entity
     * @param memoryType The memory type to erase
     */
    public static void eraseMemory(final LivingEntity entity, final MemoryModuleType<?> memoryType) {
        if (entity == null || memoryType == null) {
            return;
        }

        entity.getBrain().eraseMemory(memoryType);
    }

    /**
     * Checks if an entity has a specific memory.
     *
     * @param entity     The entity
     * @param memoryType The memory type
     * @return true if the entity has the memory
     */
    public static boolean hasMemory(final LivingEntity entity, final MemoryModuleType<?> memoryType) {
        if (entity == null || memoryType == null) {
            return false;
        }

        return entity.getBrain().hasMemoryValue(memoryType);
    }

    /**
     * Checks if a behavior can start for an entity.
     *
     * @param behavior The behavior
     * @param level    The server level
     * @param entity   The entity
     * @return true if the behavior can start
     */
    public static boolean canStart(final Behavior<? super LivingEntity> behavior, final ServerLevel level, final LivingEntity entity) {
        if (behavior == null || level == null || entity == null) {
            return false;
        }

        // Check if the behavior's memory requirements are met
        // Use reflection to access memory requirements since the method might not be public
        Map<MemoryModuleType<?>, MemoryStatus> memoryRequirements;
        try {
            java.lang.reflect.Method getMemoryRequirementsMethod = behavior.getClass().getMethod("getMemoryRequirements");
            getMemoryRequirementsMethod.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<MemoryModuleType<?>, MemoryStatus> requirements = (Map<MemoryModuleType<?>, MemoryStatus>) getMemoryRequirementsMethod.invoke(behavior);
            memoryRequirements = requirements;
        } catch (Exception e) {
            // Fallback to empty map if reflection fails
            memoryRequirements = new java.util.HashMap<>();
        }
        for (final Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : memoryRequirements.entrySet()) {
            final MemoryModuleType<?> memoryType = entry.getKey();
            final MemoryStatus requiredStatus = entry.getValue();
            final boolean hasMemory = entity.getBrain().hasMemoryValue(memoryType);

            switch (requiredStatus) {
                case REGISTERED:
                    // Memory must be registered (can be present or absent)
                    break;
                case VALUE_PRESENT:
                    if (!hasMemory) {
                        return false;
                    }
                    break;
                case VALUE_ABSENT:
                    if (hasMemory) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    /**
     * Gets the Manhattan distance between two block positions.
     *
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The Manhattan distance
     */
    public static int getManhattanDistance(final BlockPos pos1, final BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return Integer.MAX_VALUE;
        }

        return Math.abs(pos1.getX() - pos2.getX()) +
                Math.abs(pos1.getY() - pos2.getY()) +
                Math.abs(pos1.getZ() - pos2.getZ());
    }

    /**
     * Gets the Chebyshev distance between two block positions.
     *
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The Chebyshev distance
     */
    public static int getChebyshevDistance(final BlockPos pos1, final BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return Integer.MAX_VALUE;
        }

        return Math.max(Math.max(Math.abs(pos1.getX() - pos2.getX()),
                        Math.abs(pos1.getY() - pos2.getY())),
                Math.abs(pos1.getZ() - pos2.getZ()));
    }
}
