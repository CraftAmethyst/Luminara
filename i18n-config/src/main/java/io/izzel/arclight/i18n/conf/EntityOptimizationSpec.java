package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class EntityOptimizationSpec {

    @Setting("disable-entity-collisions")
    private final boolean disableEntityCollisions = false;

    // Entity cleanup removed to avoid conflicts with Paper patches
    // @Setting("entity-cleanup-enabled")
    // private boolean entityCleanupEnabled = false;

    // @Setting("entity-cleanup-threshold")
    // private int entityCleanupThreshold = 600;

    @Setting("entity-freeze-timeout")
    private final long entityFreezeTimeout = 10000;

    @Setting("reduce-entity-updates")
    private final boolean reduceEntityUpdates = true;

    // Item cleanup settings removed to avoid conflicts with Paper patches
    // @Setting("clean-valuable-items")
    // private boolean cleanValuableItems = false;

    // @Setting("item-max-age")
    // private long itemMaxAge = 6000;

    // @Setting("cleanup-notification-enabled")
    // private boolean cleanupNotificationEnabled = false;

    // @Setting("cleanup-warning-time")
    // private int cleanupWarningTime = 30;

    // Cleanup messages removed - entity cleanup functionality disabled
    // @Setting("cleanup-start-message")
    // private String cleanupStartMessage = "";

    // @Setting("cleanup-complete-message")
    // private String cleanupCompleteMessage = "";

    // @Setting("cleanup-cancelled-message")
    // private String cleanupCancelledMessage = "";

    @Setting("entity-check-interval")
    private final int entityCheckInterval = 200;

    @Setting("entity-update-distance")
    private final double entityUpdateDistance = 64.0;

    @Setting("max-entities-per-chunk")
    private final int maxEntitiesPerChunk = 100;

    @Setting("max-entities-per-type")
    private final int maxEntitiesPerType = 150;

    @Setting("chunk-entity-limit")
    private final int chunkEntityLimit = 20;

    public boolean isDisableEntityCollisions() {
        return disableEntityCollisions;
    }

    public boolean isEntityCleanupEnabled() {
        return false; // Entity cleanup disabled to avoid Paper conflicts
    }

    public int getEntityCleanupThreshold() {
        return 600; // Default value, not used
    }

    public long getEntityFreezeTimeout() {
        return entityFreezeTimeout;
    }

    public boolean isReduceEntityUpdates() {
        return reduceEntityUpdates;
    }

    public boolean isCleanValuableItems() {
        return false; // Item cleanup disabled to avoid Paper conflicts
    }

    public long getItemMaxAge() {
        return 6000; // Default value, not used
    }

    public boolean isCleanupNotificationEnabled() {
        return false; // Cleanup notifications disabled
    }

    public int getCleanupWarningTime() {
        return 30; // Default value, not used
    }

    public String getCleanupStartMessage() {
        return ""; // Cleanup messages disabled
    }

    public String getCleanupCompleteMessage() {
        return ""; // Cleanup messages disabled
    }

    public String getCleanupCancelledMessage() {
        return ""; // Cleanup messages disabled
    }

    public int getEntityCheckInterval() {
        return entityCheckInterval;
    }

    public double getEntityUpdateDistance() {
        return entityUpdateDistance;
    }

    public int getMaxEntitiesPerChunk() {
        return maxEntitiesPerChunk;
    }

    public int getMaxEntitiesPerType() {
        return maxEntitiesPerType;
    }

    public int getChunkEntityLimit() {
        return chunkEntityLimit;
    }

    // Compatibility method for legacy code
    public double getEntityActivationRange() {
        return entityUpdateDistance;
    }
}
