package io.papermc.paper.configuration;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified Paper WorldConfiguration for Luminara compatibility.
 * This provides the basic structure needed for Paper API compatibility.
 */
public class WorldConfiguration extends ConfigurationPart {

    private static final Map<ResourceLocation, WorldConfiguration> WORLD_CONFIGS = new ConcurrentHashMap<>();

    // Configuration sections
    public Entities entities = new Entities();
    public Environment environment = new Environment();
    public FeatureSeeds featureSeeds = new FeatureSeeds();
    public Fishing fishing = new Fishing();
    public Collisions collisions = new Collisions();
    public Chunks chunks = new Chunks();
    public TickRates tickRates = new TickRates();
    public Scoreboards scoreboards = new Scoreboards();
    public Anticheat anticheat = new Anticheat();
    public UnsupportedSettings unsupportedSettings = new UnsupportedSettings();

    public static WorldConfiguration get(ResourceLocation worldKey) {
        return WORLD_CONFIGS.computeIfAbsent(worldKey, k -> new WorldConfiguration());
    }

    public static void set(ResourceLocation worldKey, WorldConfiguration config) {
        WORLD_CONFIGS.put(worldKey, config);
    }

    public static class Entities extends ConfigurationPart {
        public Spawning spawning = new Spawning();
        public Behavior behavior = new Behavior();
        public SizedEntities sizedEntities = new SizedEntities();

        public static class Spawning extends ConfigurationPart {
            public boolean disableMobSpawnerSpawnEggTransformation = false;
            public boolean scanForLegacyEnderDragon = true;
            public int despawnRangesHard = 128;
            public int despawnRangesSoft = 32;
        }

        public static class Behavior extends ConfigurationPart {
            public boolean disableChestCatDetection = false;
            public boolean spawnerNerfedMobsShouldJump = false;
            public double zombieVillagerInfectionChance = -1.0;
            public boolean parrotsAreUnaffectedByPlayerMovement = false;
        }

        public static class SizedEntities extends ConfigurationPart {
            public boolean resetMobsIfTheyGetStuckInWalls = false;
        }
    }

    public static class Environment extends ConfigurationPart {
        public boolean disableExplosionKnockback = false;
        public boolean generateFlatBedrock = false;
        public FrostedIce frostedIce = new FrostedIce();
        public int treasureMapsTargetX = 200;
        public int treasureMapsTargetZ = 200;

        public static class FrostedIce extends ConfigurationPart {
            public boolean enabled = true;
            public double delay = 0.5;
        }
    }

    public static class FeatureSeeds extends ConfigurationPart {
        public int desert = -1;
        public int igloo = -1;
        public int jungle = -1;
        public int swamp = -1;
        public int monument = -1;
        public int ocean = -1;
        public int outpost = -1;
        public int endcity = -1;
        public int slime = -1;
        public int bastion = -1;
        public int fortress = -1;
        public int mansion = -1;
        public int fossil = -1;
        public int portal = -1;
    }

    public static class Fishing extends ConfigurationPart {
        public boolean disableVanillaLogic = false;
        public double transformerMinimumSize = 0.0;
        public double transformerMaximumSize = 16.0;
    }

    public static class Collisions extends ConfigurationPart {
        public boolean onlyPlayersCollide = false;
        public boolean allowVehicleCollisions = true;
        public boolean fixClimbingBypassingCrammingRule = false;
    }

    public static class Chunks extends ConfigurationPart {
        public AutoSave autoSave = new AutoSave();
        public int maxAutoSaveChunksPerTick = 24;
        public boolean fixedChunkInhabitedTime = true;
        public boolean preventMovingIntoUnloadedChunks = false;

        public static class AutoSave extends ConfigurationPart {
            public boolean enabled = true;
            public int period = 6000;
        }
    }

    public static class TickRates extends ConfigurationPart {
        public int grassSpread = 1;
        public int containerUpdate = 1;
        public int mobSpawner = 1;
        public Sensor sensor = new Sensor();

        public static class Sensor extends ConfigurationPart {
            public int villager = 1;
            public int secondaryPoi = 40;
        }
    }

    public static class Scoreboards extends ConfigurationPart {
        public boolean allowNonPlayerEntitiesOnScoreboards = false;
        public boolean useVanillaWorldScoreboardNameColoring = false;
    }

    public static class Anticheat extends ConfigurationPart {
        public AntiXray antiXray = new AntiXray();
        public Obfuscation obfuscation = new Obfuscation();

        public static class AntiXray extends ConfigurationPart {
            public boolean enabled = false;
            public int engineMode = 1;
            public int maxBlockHeight = 64;
            public int updateRadius = 2;
            public boolean lavaObscures = false;
            public boolean usePermission = false;
        }

        public static class Obfuscation extends ConfigurationPart {
            public Items items = new Items();

            public static class Items extends ConfigurationPart {
                public boolean hideItemmeta = false;
                public boolean hideDurability = false;
            }
        }
    }

    public static class UnsupportedSettings extends ConfigurationPart {
        public boolean allowHeadlessPistons = false;
        public boolean allowGrindstoneOverstacking = false;
        public boolean allowPermanentBlockBreakExploits = false;
        public boolean allowPistonDuplication = false;
        public boolean performUsernameValidation = true;
        public CompressionFormat compressionFormat = CompressionFormat.ZLIB;

        public enum CompressionFormat {
            GZIP, ZLIB, NONE
        }
    }
}
