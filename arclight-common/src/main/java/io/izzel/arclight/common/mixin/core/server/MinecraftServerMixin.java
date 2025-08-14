package io.izzel.arclight.common.mixin.core.server;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverterRegistry;
import com.mojang.datafixers.DataFixer;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.bridge.core.command.ICommandSourceBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.common.mod.server.BukkitRegistry;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.BukkitOptionParser;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import it.unimi.dsi.fastutil.longs.LongIterator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.internal.BrandingControl;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.scoreboard.CraftScoreboardManager;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.slf4j.Logger;
import org.spigotmc.WatchdogThread;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements MinecraftServerBridge, ICommandSourceBridge {

    private static final ExecutorService ASYNC_SAVE_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "Async-World-Save-Thread");
        thread.setDaemon(true);
        return thread;
    });
    private static final int TPS = 20;
    private static final int TICK_TIME = 1000000000 / TPS;
    private static final int SAMPLE_INTERVAL = 100;
    private static final org.apache.logging.log4j.Logger ARCLIGHT_LOGGER = ArclightI18nLogger.getLogger("MinecraftServer");
    @Shadow
    @Final
    static Logger LOGGER;
    private static int currentTick = (int) (System.currentTimeMillis() / 50);
    public final double[] recentTps = new double[3];
    private final Object stopLock = new Object();
    @Shadow
    @Final
    public Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow
    @Final
    public Executor executor;
    @Shadow
    public MinecraftServer.ReloadableResources resources;
    public WorldLoader.DataLoadContext worldLoader;
    public CraftServer server;
    public OptionSet options;
    public ConsoleCommandSender console;
    public RemoteConsoleCommandSender remoteConsole;
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public int autosavePeriod;
    public Commands vanillaCommandDispatcher;
    @Shadow
    protected long nextTickTime;
    @Shadow
    protected WorldData worldData;
    @Shadow
    @Final
    protected Services services;
    // @formatter:off
    @Shadow private int tickCount;
    @Shadow private ServerStatus status;
    @Shadow @Nullable private String motd;
    @Shadow private volatile boolean running;
    @Shadow private long lastOverloadWarning;
    @Shadow private boolean mayHaveDelayedTasks;
    @Shadow private long delayedTasksMaxNextTickTime;
    @Shadow private volatile boolean isReady;
    @Shadow private boolean stopped;
    @Shadow private ProfilerFiller profiler;
    @Shadow private float averageTickTime;
    @Shadow @Final private PackRepository packRepository;
    @Shadow @Final private ServerFunctionManager functionManager;
    @Shadow @Final private StructureTemplateManager structureTemplateManager;
    @Shadow private boolean debugCommandProfilerDelayStart;
    @Shadow @Nullable private MinecraftServer.TimeProfiler debugCommandProfiler;
    @Shadow @Nullable private ServerStatus.Favicon statusIcon;
    private boolean forceTicks;
    private boolean hasStopped = false;
    @Unique
    private transient ServerLevel arclight$capturedLevel;
    public MinecraftServerMixin(String name) {
        super(name);
    }

    @Shadow private static void setInitialSpawn(ServerLevel p_177897_, ServerLevelData p_177898_, boolean p_177899_, boolean p_177900_) { }

    @Shadow private static DataPackConfig getSelectedPacks(PackRepository p_129818_) { return null; }

    @Shadow private static CrashReport constructOrExtractCrashReport(Throwable p_206569_) { return null; }

    private static double calcTps(double avg, double exp, double tps) {
        return (avg * exp) + (tps * (1 - exp));
    }

    private static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @Shadow protected abstract boolean initServer() throws IOException;

    @Shadow public abstract void tickServer(BooleanSupplier hasTimeLeft);

    @Shadow protected abstract boolean haveTime();

    @Shadow protected abstract void waitUntilNextTick();

    @Shadow protected abstract void onServerCrash(CrashReport report);
    // @formatter:on

    @Shadow
    public abstract File getServerDirectory();

    @Shadow
    public abstract void stopServer();

    @Shadow
    public abstract void onServerExit();

    @Shadow
    public abstract Commands getCommands();

    @Shadow
    protected abstract void updateMobSpawningFlags();

    @Shadow
    public abstract ServerLevel overworld();

    @Shadow
    protected abstract void setupDebugLevel(WorldData p_240778_1_);

    @Shadow(remap = false)
    @Deprecated
    public abstract void markWorldsDirty();

    @Shadow
    public abstract boolean isSpawningMonsters();

    @Shadow
    public abstract boolean isSpawningAnimals();

    @Shadow
    protected abstract void startMetricsRecordingTick();

    @Shadow
    protected abstract void endMetricsRecordingTick();

    @Shadow
    public abstract SystemReport fillSystemReport(SystemReport p_177936_);

    @Shadow
    public abstract boolean isDedicatedServer();

    @Shadow
    public abstract int getFunctionCompilationLevel();

    @Shadow
    public abstract RegistryAccess.Frozen registryAccess();

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    public abstract boolean enforceSecureProfile();

    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();

    @Shadow
    protected abstract ServerStatus buildServerStatus();

    @Shadow
    protected abstract Optional<ServerStatus.Favicon> loadStatusIcon();

    public boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Override
    public boolean bridge$hasStopped() {
        return this.hasStopped();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void arclight$loadOptions(Thread p_236723_, LevelStorageSource.LevelStorageAccess p_236724_, PackRepository p_236725_, WorldStem worldStem, Proxy p_236727_, DataFixer p_236728_, Services p_236729_, ChunkProgressListenerFactory p_236730_, CallbackInfo ci) {
        String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
        OptionParser parser = new BukkitOptionParser();
        try {
            options = parser.parse(arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
        this.worldLoader = ArclightCaptures.getDataLoadContext();
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected void runServer() {
        try {
            ARCLIGHT_LOGGER.info("server.starting");

            // Luminara - Initialize Data Converter Registry
            try {
                MCDataConverterRegistry.init();
                ARCLIGHT_LOGGER.info("Initialized Minecraft Data Converter Registry");
            } catch (Exception e) {
                ARCLIGHT_LOGGER.error("Failed to initialize Data Converter Registry", e);
            }

            // Luminara - Validate Paper compatibility before server start
            io.izzel.arclight.common.optimization.paper.PaperCompatibleOptimizer.logCompatibilityStatus();

            if (!this.initServer()) {
                throw new IllegalStateException("Failed to initialize server");
            }
            ServerLifecycleHooks.handleServerStarted((MinecraftServer) (Object) this);
            long endTime = Util.getMillis();
            this.nextTickTime = endTime;
            this.statusIcon = this.loadStatusIcon().orElse(null);
            this.status = this.buildServerStatus();

            Arrays.fill(recentTps, 20);
            long curTime, tickSection = Util.getMillis(), tickCount = 1;

            while (this.running) {
                long i = (curTime = Util.getMillis()) - this.nextTickTime;
                if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                    long j = i / 50L;

                    if (server.getWarnOnOverload()) {
                        ARCLIGHT_LOGGER.warn("server.overload-warning", i, j);
                    }

                    this.nextTickTime += j * 50L;
                    this.lastOverloadWarning = this.nextTickTime;
                }

                if (tickCount++ % SAMPLE_INTERVAL == 0) {
                    double currentTps = 1E3 / (curTime - tickSection) * SAMPLE_INTERVAL;
                    recentTps[0] = calcTps(recentTps[0], 0.92, currentTps); // 1/exp(5sec/1min)
                    recentTps[1] = calcTps(recentTps[1], 0.9835, currentTps); // 1/exp(5sec/5min)
                    recentTps[2] = calcTps(recentTps[2], 0.9945, currentTps); // 1/exp(5sec/15min)
                    tickSection = curTime;
                }

                currentTick = (int) (System.currentTimeMillis() / 50);

                if (this.debugCommandProfilerDelayStart) {
                    this.debugCommandProfilerDelayStart = false;
                    this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                }

                this.nextTickTime += 50L;
                this.startMetricsRecordingTick();
                this.profiler.push("tick");
                this.tickServer(this::haveTime);
                this.profiler.popPush("nextTickWait");
                this.mayHaveDelayedTasks = true;
                this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                this.waitUntilNextTick();
                this.profiler.pop();
                this.endMetricsRecordingTick();
                this.isReady = true;
                JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
            }
            ARCLIGHT_LOGGER.info("server.stopping");
            ServerLifecycleHooks.handleServerStopping((MinecraftServer) (Object) this);
            ServerLifecycleHooks.expectServerStopped(); // has to come before finalTick to avoid race conditions
        } catch (Throwable throwable1) {
            ARCLIGHT_LOGGER.error("server.unexpected-exception", throwable1);
            CrashReport crashreport = constructOrExtractCrashReport(throwable1);
            this.fillSystemReport(crashreport.getSystemReport());

            // Use the new crash handler to determine if we should continue
            boolean shouldContinue = io.izzel.arclight.common.mod.util.ArclightCrashHandler.handleCrash(
                    throwable1, crashreport, this.getServerDirectory()
            );

            if (!shouldContinue) {
                // Original behavior - stop the server
                File file1 = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                if (crashreport.saveToFile(file1)) {
                    ARCLIGHT_LOGGER.error("server.crash-report-saved", file1.getAbsolutePath());
                } else {
                    ARCLIGHT_LOGGER.error("server.crash-report-failed");
                }
                this.onServerCrash(crashreport);
            } else {
                // Continue running - log but don't crash
                ARCLIGHT_LOGGER.warn("server.continuing-after-crash");
            }

            net.minecraftforge.server.ServerLifecycleHooks.expectServerStopped(); // Forge: Has to come before MinecraftServer#onServerCrash to avoid race conditions
            this.onServerCrash(crashreport);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable throwable) {
                ARCLIGHT_LOGGER.error("server.stop-exception", throwable);
            } finally {
                if (this.services.profileCache() != null) {
                    this.services.profileCache().clearExecutor();
                }
                WatchdogThread.doStop();
                // Shutdown async world save executor
                arclight$shutdownAsyncSaveExecutor();
                ServerLifecycleHooks.handleServerStopped((MinecraftServer) (Object) this);
                ARCLIGHT_LOGGER.info("server.stopped");
                this.onServerExit();
            }
        }
    }

    @Inject(method = "stopServer", cancellable = true, at = @At("HEAD"))
    public void arclight$setStopped(CallbackInfo ci) {
        synchronized (stopLock) {
            if (hasStopped) {
                ci.cancel();
                return;
            }
            hasStopped = true;
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void arclight$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"))
    public void arclight$asyncWorldSave(CallbackInfo ci) {
        if (io.izzel.arclight.i18n.ArclightConfig.spec().getAsyncWorldSave().isEnabled()) {
            ARCLIGHT_LOGGER.info("server.async-world-save.starting-shutdown");
            arclight$saveAllWorldsAsync(true, true, true); // suppressLog = true to avoid duplicate message
        }

        // Luminara - Paper optimization cleanup removed (conflicts with Paper patches)
    }

    @Inject(method = "createLevels", at = @At("RETURN"))
    public void arclight$enablePlugins(ChunkProgressListener p_240787_1_, CallbackInfo ci) {
        BukkitRegistry.unlockRegistries();
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
        BukkitRegistry.lockRegistries();
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));


    }

    private void executeModerately() {
        this.runAllTasks();
        this.bridge$drainQueuedTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Override
    public void bridge$drainQueuedTasks() {
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void arclight$forceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    @Inject(method = "createLevels", at = @At(value = "NEW", ordinal = 0, target = "net/minecraft/server/level/ServerLevel"))
    private void arclight$registerEnv(ChunkProgressListener p_240787_1_, CallbackInfo ci) {
        BukkitRegistry.registerEnvironments(this.registryAccess().registryOrThrow(Registries.LEVEL_STEM));
    }

    @ModifyArg(method = "createLevels", index = 1, at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object arclight$worldInitCapture(Object value) {
        arclight$capturedLevel = (ServerLevel) value;
        return value;
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void arclight$worldInit(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        ServerLevel serverWorld = arclight$capturedLevel;
        arclight$capturedLevel = null;
        if (serverWorld != null) {
            this.levels.put(serverWorld.dimension(), serverWorld);
            // Luminara - Use Paper-compatible optimization instead
            io.izzel.arclight.common.optimization.paper.PaperCompatibleOptimizer.optimizeWorldCompatibly(serverWorld);

            if (((CraftServer) Bukkit.getServer()).scoreboardManager == null) {
                ((CraftServer) Bukkit.getServer()).scoreboardManager = new CraftScoreboardManager((MinecraftServer) (Object) this, serverWorld.getScoreboard());
            }
            if (((WorldBridge) serverWorld).bridge$getGenerator() != null) {
                ((WorldBridge) serverWorld).bridge$getWorld().getPopulators().addAll(
                        ((WorldBridge) serverWorld).bridge$getGenerator().getDefaultPopulators(
                                ((WorldBridge) serverWorld).bridge$getWorld()));
            }
            Bukkit.getPluginManager().callEvent(new WorldInitEvent(((WorldBridge) serverWorld).bridge$getWorld()));
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void prepareLevels(ChunkProgressListener listener) {
        // Luminara - World creation optimization removed to avoid conflicts with Paper patches
        // Using standard spawn area preparation instead

        ServerLevel serverworld = this.overworld();
        this.forceTicks = true;
        ARCLIGHT_LOGGER.info("world.loading", serverworld.dimension().location());
        ARCLIGHT_LOGGER.info("server.preparing-start-region", serverworld.dimension().location());
        BlockPos blockpos = serverworld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverworld.getChunkSource();
        this.nextTickTime = Util.getMillis();

        // Use standard spawn radius (11) instead of config value to avoid Paper conflicts
        int spawnRadius = 11;
        serverchunkprovider.addRegionTicket(TicketType.START, new ChunkPos(blockpos), spawnRadius, Unit.INSTANCE);
        // Always load spawn chunks to maintain compatibility
        int targetChunks = (spawnRadius * 2 + 1) * (spawnRadius * 2 + 1);
        while (serverchunkprovider.getTickingGenerated() < targetChunks) {
            this.executeModerately();
        }

        this.executeModerately();

        for (ServerLevel serverWorld : this.levels.values()) {
            if (((WorldBridge) serverWorld).bridge$getWorld().getKeepSpawnInMemory()) {
                ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
                if (forcedchunkssavedata != null) {
                    LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

                    while (longiterator.hasNext()) {
                        long i = longiterator.nextLong();
                        ChunkPos chunkpos = new ChunkPos(i);
                        serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
                    }
                    net.minecraftforge.common.world.ForgeChunkManager.reinstatePersistentChunks(serverWorld, forcedchunkssavedata);
                }
            }
            Bukkit.getPluginManager().callEvent(new WorldLoadEvent(((WorldBridge) serverWorld).bridge$getWorld()));
            ARCLIGHT_LOGGER.info("world.loaded", ((WorldBridge) serverWorld).bridge$getWorld().getName());
        }

        this.executeModerately();
        listener.stop();
        this.updateMobSpawningFlags();
        this.forceTicks = false;
    }

    // bukkit methods
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        // Luminara - World creation optimization removed to avoid conflicts with Paper patches
        // Using standard world initialization instead

        ARCLIGHT_LOGGER.info("world.creating", ((WorldBridge) serverWorld).bridge$getWorld().getName());

        boolean flag = saveData.isDebugWorld();
        if (((WorldBridge) serverWorld).bridge$getGenerator() != null) {
            ((WorldBridge) serverWorld).bridge$getWorld().getPopulators().addAll(
                    ((WorldBridge) serverWorld).bridge$getGenerator().getDefaultPopulators(
                            ((WorldBridge) serverWorld).bridge$getWorld()));
        }

        // Always setup world border (Paper handles optimization internally)
        WorldBorder worldborder = serverWorld.getWorldBorder();
        worldborder.applySettings(worldInfo.getWorldBorder());

        if (!worldInfo.isInitialized()) {
            try {
                // Always set initial spawn (Paper handles optimization internally)
                setInitialSpawn(serverWorld, worldInfo, worldOptions.generateBonusChest(), flag);
                worldInfo.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverWorld.fillReportDetails(crashreport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashreport);
            }
            worldInfo.setInitialized(true);
        }

        ARCLIGHT_LOGGER.info("world.created", ((WorldBridge) serverWorld).bridge$getWorld().getName());
    }

    // bukkit methods
    public void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
        this.markWorldsDirty();
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(serverWorld));
        if (!((WorldBridge) serverWorld).bridge$getWorld().getKeepSpawnInMemory()) {
            return;
        }
        this.forceTicks = true;
        ARCLIGHT_LOGGER.info("server.preparing-start-region", serverWorld.dimension().location());
        BlockPos blockpos = serverWorld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverWorld.getChunkSource();
        this.nextTickTime = Util.getMillis();
        serverchunkprovider.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

        while (serverchunkprovider.getTickingGenerated() < 441) {
            this.executeModerately();
        }

        this.executeModerately();

        ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
        if (forcedchunkssavedata != null) {
            LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

            while (longiterator.hasNext()) {
                long i = longiterator.nextLong();
                ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
            }
            net.minecraftforge.common.world.ForgeChunkManager.reinstatePersistentChunks(serverWorld, forcedchunkssavedata);
        }
        this.executeModerately();
        listener.stop();
        // this.updateMobSpawningFlags();
        serverWorld.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        this.forceTicks = false;
    }

    // bukkit callbacks
    public void addLevel(ServerLevel level) {
        this.levels.put(level.dimension(), level);
        this.markWorldsDirty();
    }

    public void removeLevel(ServerLevel level) {
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level));
        this.levels.remove(level.dimension());
        this.markWorldsDirty();
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    public void arclight$runScheduler(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        ArclightConstants.currentTick = (int) (System.currentTimeMillis() / 50);
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount);
        this.bridge$drainQueuedTasks();
    }

    @Inject(method = "saveAllChunks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private void arclight$skipSave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.levels.isEmpty());
    }

    /**
     * Asynchronously save all worlds
     * Based on Mohist's DimensionDataStorage async save implementation
     */
    private void arclight$saveAllWorldsAsync(boolean suppressLog, boolean flush, boolean forced) {
        List<CompletableFuture<Void>> saveTasks = new ArrayList<>();

        if (!suppressLog) {
            ARCLIGHT_LOGGER.info("server.async-world-save.starting");
        }

        // Create async save tasks for each world
        for (ServerLevel level : this.levels.values()) {
            CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> {
                try {
                    arclight$saveWorldAsync(level, suppressLog, flush, forced);
                } catch (Exception e) {
                    ARCLIGHT_LOGGER.error("server.world-save.failed", level.dimension().location(), e);
                }
            }, ASYNC_SAVE_EXECUTOR);

            saveTasks.add(saveTask);
        }

        // Wait for all save tasks to complete
        if (!saveTasks.isEmpty()) {
            try {
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        saveTasks.toArray(new CompletableFuture[0])
                );

                int timeoutSeconds = io.izzel.arclight.i18n.ArclightConfig.spec().getAsyncWorldSave().getTimeoutSeconds();
                allTasks.get(timeoutSeconds, TimeUnit.SECONDS);

                if (!suppressLog) {
                    ARCLIGHT_LOGGER.info("server.world-save.all-successful");
                }
            } catch (Exception e) {
                ARCLIGHT_LOGGER.warn("server.async-world-save.timeout-or-failed", e);
            }
        }
    }

    /**
     * Asynchronously save a single world
     */
    private void arclight$saveWorldAsync(ServerLevel level, boolean suppressLog, boolean flush, boolean forced) {
        if (!suppressLog) {
            ARCLIGHT_LOGGER.debug("world.saving", level.dimension().location());
        }

        try {
            // Save world data
            level.save(null, flush, level.noSave && !forced);

            if (!suppressLog) {
                ARCLIGHT_LOGGER.debug("world.saved-successfully", level.dimension().location());
            }
        } catch (Exception e) {
            ARCLIGHT_LOGGER.error("world.save-error", level.dimension().location(), e);
        }
    }

    /**
     * Shutdown async save executor
     */
    private void arclight$shutdownAsyncSaveExecutor() {
        ASYNC_SAVE_EXECUTOR.shutdown();
        try {
            if (!ASYNC_SAVE_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                ASYNC_SAVE_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ASYNC_SAVE_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Inject(method = "desc=/V$/", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;setSelected(Ljava/util/Collection;)V"))
    private void arclight$syncCommand(CallbackInfo ci) {
        this.server.syncCommands();
    }

    /**
     * @author IzzelAliz
     * @reason our branding, no one should fuck this up
     */
    @DontObfuscate
    @Overwrite
    public String getServerModName() {
        return BrandingControl.getServerBranding() + " luminara/" + ArclightVersion.current().getReleaseName();
    }

    @Override
    public void bridge$setAutosavePeriod(int autosavePeriod) {
        this.autosavePeriod = autosavePeriod;
    }

    @Override
    public void bridge$setConsole(ConsoleCommandSender console) {
        this.console = console;
    }

    @Override
    public void bridge$setServer(CraftServer server) {
        this.server = server;
    }

    @Override
    public RemoteConsoleCommandSender bridge$getRemoteConsole() {
        return remoteConsole;
    }

    @Override
    public void bridge$setRemoteConsole(RemoteConsoleCommandSender sender) {
        this.remoteConsole = sender;
    }

    @Override
    public void bridge$queuedProcess(Runnable runnable) {
        processQueue.add(runnable);
    }

    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return console;
    }

    @Override
    public CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitSender(wrapper);
    }

    @Override
    public Commands bridge$getVanillaCommands() {
        return this.vanillaCommandDispatcher;
    }

    public boolean isDebugging() {
        return false;
    }


}

