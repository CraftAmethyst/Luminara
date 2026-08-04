package io.izzel.arclight.common.mod.util.remapper;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import io.izzel.arclight.api.PluginPatcher;
import io.izzel.arclight.common.mod.ArclightMod;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.tools.product.Product;
import io.izzel.tools.product.Product3;
import io.izzel.tools.product.Product5;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarFile;
import net.minecraftforge.fml.ModList;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public abstract class ArclightClassCache implements AutoCloseable {

    private static final Marker MARKER = MarkerManager.getMarker("CLCACHE");
    private static final ArclightClassCache INSTANCE = new Impl();

    public static ArclightClassCache instance() {
        return INSTANCE;
    }

    public abstract CacheSegment makeSegment(URLConnection connection)
        throws IOException;

    public abstract void save() throws IOException;

    public interface CacheSegment {
        Optional<byte[]> findByName(String name, ArclightRemapConfig config)
            throws IOException;

        void addToCache(String name, byte[] value, ArclightRemapConfig config);

        void save() throws IOException;
    }

    private static class Impl extends ArclightClassCache {

        private static final int SPEC_VERSION = 2;

        private final boolean enabled = ArclightConfig.spec()
            .getOptimization()
            .isCachePluginClass();
        private final ConcurrentHashMap<String, JarSegment> map =
            new ConcurrentHashMap<>();
        private final Path basePath = Paths.get(".arclight/class_cache");
        private ScheduledExecutorService executor;

        public Impl() {
            if (!enabled) return;
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setName("arclight class cache saving thread");
                thread.setDaemon(true);
                return thread;
            });
            executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        this.save();
                    } catch (IOException e) {
                        ArclightMod.LOGGER.error(
                            MARKER,
                            "Failed to save class cache",
                            e
                        );
                    }
                },
                1,
                10,
                TimeUnit.MINUTES
            );
            try {
                if (Files.isRegularFile(basePath)) {
                    Files.delete(basePath);
                }
                if (!Files.isDirectory(basePath)) {
                    Files.createDirectories(basePath);
                }
                String current = currentVersionInfo();
                String store;
                Path version = basePath.resolve(".version");
                if (Files.exists(version)) {
                    store = Files.readString(version);
                } else {
                    store = null;
                }
                boolean obsolete = !Objects.equals(current, store);
                Path index = basePath.resolve("index");
                if (obsolete) {
                    FileUtils.deleteDirectory(index.toFile());
                }
                if (!Files.exists(index)) {
                    Files.createDirectories(index);
                }
                Path blob = basePath.resolve("blob");
                if (obsolete) {
                    FileUtils.deleteDirectory(blob.toFile());
                }
                if (!Files.exists(blob)) {
                    Files.createDirectories(blob);
                }
                if (obsolete) {
                    Files.deleteIfExists(version);
                    Files.writeString(
                        version,
                        current,
                        StandardOpenOption.CREATE
                    );
                    ArclightMod.LOGGER.info(
                        MARKER,
                        "Obsolete plugin class cache is cleared"
                    );
                }
            } catch (IOException e) {
                ArclightMod.LOGGER.error(
                    MARKER,
                    "Failed to initialize class cache",
                    e
                );
            }
            Thread thread = new Thread(
                () -> {
                    try {
                        this.close();
                    } catch (Exception e) {
                        ArclightMod.LOGGER.error(
                            MARKER,
                            "Failed to close class cache",
                            e
                        );
                    }
                },
                "arclight class cache cleanup"
            );
            thread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(thread);
        }

        private static String currentVersionInfo() {
            var builder = new StringBuilder();
            var arclight = ModList.get()
                .getModContainerById("luminara")
                .orElseThrow(IllegalStateException::new)
                .getModInfo()
                .getVersion()
                .toString();
            builder.append(arclight);
            builder.append("Arclight class cache").append(", ");
            builder.append("spec=").append(SPEC_VERSION).append(", ");
            builder.append("arclight=").append(arclight).append(", ");
            builder.append("patcher=[");
            for (PluginPatcher patcher : ArclightRemapper.INSTANCE.getPatchers()) {
                builder
                    .append('\0')
                    .append(patcher.getClass().getName())
                    .append('\0')
                    .append(patcher.version())
                    .append(", ");
            }
            builder.append("]");
            return builder.toString();
        }

        @Override
        public CacheSegment makeSegment(URLConnection connection)
            throws IOException {
            if (enabled && connection instanceof JarURLConnection) {
                JarFile file = ((JarURLConnection) connection).getJarFile();
                return this.map.computeIfAbsent(
                    file.getName(),
                    LamdbaExceptionUtils.rethrowFunction(JarSegment::new)
                );
            } else {
                return new EmptySegment();
            }
        }

        @Override
        public void save() throws IOException {
            if (enabled) {
                for (CacheSegment segment : map.values()) {
                    segment.save();
                }
            }
        }

        @Override
        public void close() throws Exception {
            if (enabled) {
                save();
                executor.shutdownNow();
            }
        }

        private static class EmptySegment implements CacheSegment {

            @Override
            public Optional<byte[]> findByName(
                String name,
                ArclightRemapConfig config
            ) {
                return Optional.empty();
            }

            @Override
            public void addToCache(
                String name,
                byte[] value,
                ArclightRemapConfig config
            ) {}

            @Override
            public void save() {}
        }

        private class JarSegment implements CacheSegment {

            private final Map<
                String,
                Product3<Long, Integer, ArclightRemapConfig>
            > rangeMap = new ConcurrentHashMap<>();
            private final ConcurrentLinkedQueue<
                Product5<String, byte[], Long, Integer, ArclightRemapConfig>
            > savingQueue = new ConcurrentLinkedQueue<>();
            private final AtomicLong sizeAllocator;
            private final Path indexPath, blobPath;

            private JarSegment(String fileName) throws IOException {
                Path jarFile = new File(fileName).toPath();
                Hasher hasher = Hashing.sha256().newHasher();
                hasher.putBytes(Files.readAllBytes(jarFile));
                String hash = hasher.hash().toString();
                this.indexPath = basePath.resolve("index").resolve(hash);
                this.blobPath = basePath.resolve("blob").resolve(hash);
                if (!Files.exists(indexPath)) {
                    Files.createFile(indexPath);
                }
                if (!Files.exists(blobPath)) {
                    Files.createFile(blobPath);
                }
                sizeAllocator = new AtomicLong(Files.size(blobPath));
                read();
            }

            @Override
            public Optional<byte[]> findByName(
                String name,
                ArclightRemapConfig config
            ) throws IOException {
                Product3<Long, Integer, ArclightRemapConfig> product =
                    rangeMap.get(name);
                if (product != null) {
                    long off = product._1;
                    int len = product._2;
                    var cfg = product._3;
                    if (!cfg.equals(config)) {
                        return Optional.empty();
                    }
                    try (
                        SeekableByteChannel channel = Files.newByteChannel(
                            blobPath
                        )
                    ) {
                        channel.position(off);
                        ByteBuffer buffer = ByteBuffer.allocate(len);
                        channel.read(buffer);
                        return Optional.of(buffer.array());
                    }
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public void addToCache(
                String name,
                byte[] value,
                ArclightRemapConfig config
            ) {
                int len = value.length;
                long off = sizeAllocator.getAndAdd(len);
                savingQueue.add(
                    Product.of(name, value, off, len, config.copy())
                );
            }

            @Override
            public synchronized void save() throws IOException {
                if (savingQueue.isEmpty()) return;
                List<
                    Product5<String, byte[], Long, Integer, ArclightRemapConfig>
                > list = new ArrayList<>();
                while (!savingQueue.isEmpty()) {
                    list.add(savingQueue.poll());
                }
                try (
                    OutputStream outIndex = Files.newOutputStream(
                        indexPath,
                        StandardOpenOption.APPEND
                    );
                    DataOutputStream dataOutIndex = new DataOutputStream(
                        outIndex
                    );
                    SeekableByteChannel channel = Files.newByteChannel(
                        blobPath,
                        StandardOpenOption.WRITE
                    )
                ) {
                    for (Product5<
                        String,
                        byte[],
                        Long,
                        Integer,
                        ArclightRemapConfig
                    > product : list) {
                        channel.position(product._3);
                        channel.write(ByteBuffer.wrap(product._2));
                        dataOutIndex.writeUTF(product._1);
                        dataOutIndex.writeLong(product._3);
                        dataOutIndex.writeInt(product._4);
                        product._5.write(dataOutIndex);
                        rangeMap.put(
                            product._1,
                            Product.of(product._3, product._4, product._5)
                        );
                    }
                }
            }

            private synchronized void read() throws IOException {
                try (
                    InputStream inputStream = Files.newInputStream(indexPath);
                    DataInputStream dataIn = new DataInputStream(inputStream)
                ) {
                    while (dataIn.available() > 0) {
                        String name = dataIn.readUTF();
                        long off = dataIn.readLong();
                        int len = dataIn.readInt();
                        var cfg = ArclightRemapConfig.read(dataIn);
                        rangeMap.put(name, Product.of(off, len, cfg));
                    }
                }
            }
        }
    }
}
