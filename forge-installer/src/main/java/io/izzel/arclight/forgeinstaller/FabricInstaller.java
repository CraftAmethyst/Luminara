package io.izzel.arclight.forgeinstaller;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class FabricInstaller {

    private static final String DEFAULT_FABRIC_MAIN_CLASS = "net.fabricmc.loader.impl.launch.server.FabricServerLauncher";

    @SuppressWarnings("unused")
    public static Map.Entry<String, List<Path>> applicationInstall() throws Exception {
        try (InputStream stream = FabricInstaller.class.getResourceAsStream("/META-INF/installer.json")) {
            if (stream == null) {
                throw new IllegalStateException("Missing /META-INF/installer.json");
            }
            InstallInfo installInfo = new Gson().fromJson(new InputStreamReader(stream), InstallInfo.class);
            if (installInfo == null || installInfo.installer == null || installInfo.installer.fabricLoader == null) {
                throw new IllegalStateException("Invalid installer metadata for Fabric");
            }

            var logger = (Consumer<String>) System.out::println;
            Path loaderPath = Paths.get("libraries", "net", "fabricmc", "fabric-loader",
                    installInfo.installer.fabricLoader, "fabric-loader-" + installInfo.installer.fabricLoader + ".jar");
            Path serverPath = Paths.get("libraries", "net", "minecraft", "server",
                    installInfo.installer.minecraft, "server-" + installInfo.installer.minecraft + ".jar");

            // Optional extra coordinates (kept for forward compatibility with richer metadata).
            downloadAll(checkMavenNoSource(installInfo.fabricDeps()), logger);

            boolean installLoader = !Files.exists(loaderPath) || fabricClasspathMissing(loaderPath);
            if (installLoader) {
                logger.accept("Installing Fabric loader...");
                String coord = "net.fabricmc:fabric-loader:" + installInfo.installer.fabricLoader;
                String target = "libraries/" + Util.mavenToPath(coord);
                Path downloadedLoader = new MavenDownloader(
                        Mirrors.getMavenRepo(), coord, target, installInfo.installer.fabricLoaderHash
                ).get();
                logger.accept("Downloaded " + downloadedLoader);
                downloadAll(checkMaven(fabricDeps(downloadedLoader)), logger);
            }

            if (!Files.exists(serverPath)) {
                logger.accept("Downloading Minecraft server jar...");
                MinecraftData data = downloadMinecraftData(installInfo.installer.minecraft, logger);
                if (data == null) {
                    throw new IllegalStateException("Failed to resolve Minecraft version metadata: " + installInfo.installer.minecraft);
                }
                Path downloadedServer = new FileDownloader(
                        String.format(data.serverUrl(), installInfo.installer.minecraft),
                        serverPath.toString(),
                        data.serverHash()
                ).get();
                logger.accept("Downloaded " + downloadedServer);
            }

            return classpath(installInfo, loaderPath);
        }
    }

    private static Map.Entry<String, List<Path>> classpath(InstallInfo info, Path loaderPath) throws Exception {
        Path serverPath = Paths.get("libraries", "net", "minecraft", "server",
                info.installer.minecraft, "server-" + info.installer.minecraft + ".jar").toAbsolutePath();
        System.setProperty("fabric.gameJarPath", serverPath.toString());

        List<Path> libs = new ArrayList<>();
        for (String dep : fabricDeps(loaderPath).keySet()) {
            libs.add(Paths.get("libraries", Util.mavenToPath(dep)));
        }
        for (String dep : info.fabricDeps().keySet()) {
            libs.add(Paths.get("libraries", Util.mavenToPath(dep)));
        }
        libs.add(loaderPath.toAbsolutePath());
        libs = libs.stream().distinct().collect(Collectors.toList());

        try (var file = new JarFile(loaderPath.toFile())) {
            String mainClass = file.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass == null || mainClass.isBlank()) {
                mainClass = DEFAULT_FABRIC_MAIN_CLASS;
            }
            return Map.entry(mainClass, libs);
        }
    }

    private static boolean fabricClasspathMissing(Path loaderPath) throws Exception {
        for (String dep : fabricDeps(loaderPath).keySet()) {
            if (!Files.exists(Paths.get("libraries", Util.mavenToPath(dep)))) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Map.Entry<String, String>> fabricDeps(Path loaderPath) throws Exception {
        var result = new LinkedHashMap<String, Map.Entry<String, String>>();
        try (var file = new JarFile(loaderPath.toFile())) {
            var entry = file.getEntry("fabric-installer.json");
            if (entry == null) {
                throw new IllegalStateException("fabric-installer.json missing in " + loaderPath);
            }
            try (var stream = file.getInputStream(entry)) {
                var root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                var libs = root.getAsJsonObject("libraries");
                for (var blockName : List.of("common", "server")) {
                    var block = libs.getAsJsonArray(blockName);
                    for (var element : block) {
                        var obj = element.getAsJsonObject();
                        var name = obj.get("name").getAsString();
                        var url = obj.get("url").getAsString() + Util.mavenToPath(name);
                        var sha1 = obj.get("sha1").getAsString();
                        result.put(name, new AbstractMap.SimpleImmutableEntry<>(sha1, url));
                    }
                }
            }
        }
        return result;
    }

    private static MinecraftData downloadMinecraftData(String minecraftVersion, Consumer<String> logger) {
        logger.accept("Resolving Minecraft version manifest...");
        for (Map.Entry<String, String> mirror : Mirrors.getVersionManifest()) {
            try (var stream = FileDownloader.read(mirror.getValue())) {
                var manifest = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                var versions = manifest.getAsJsonArray("versions");
                for (var version : versions) {
                    var obj = version.getAsJsonObject();
                    if (Objects.equals(obj.get("id").getAsString(), minecraftVersion)) {
                        var detailUrl = obj.get("url").getAsString();
                        try (var detailStream = FileDownloader.read(detailUrl)) {
                            var detail = JsonParser.parseString(new String(detailStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                            var server = detail.getAsJsonObject("downloads").getAsJsonObject("server");
                            return new MinecraftData(
                                    mirror.getKey(),
                                    Mirrors.mapMojangMirror(server.get("url").getAsString(), mirror.getKey()),
                                    server.get("sha1").getAsString()
                            );
                        }
                    }
                }
            } catch (Exception ex) {
                logger.accept("Failed to load version manifest from " + mirror.getKey() + ": " + ex.getMessage());
            }
        }
        return null;
    }

    private static void downloadAll(List<Supplier<Path>> suppliers, Consumer<String> logger) {
        for (Supplier<Path> supplier : suppliers) {
            Path path = supplier.get();
            logger.accept("Downloaded " + path);
        }
    }

    private static List<Supplier<Path>> checkMavenNoSource(Map<String, String> map) {
        var deps = new LinkedHashMap<String, Map.Entry<String, String>>();
        if (map == null) {
            return List.of();
        }
        for (var entry : map.entrySet()) {
            deps.put(entry.getKey(), new AbstractMap.SimpleImmutableEntry<>(entry.getValue(), null));
        }
        return checkMaven(deps);
    }

    private static List<Supplier<Path>> checkMaven(Map<String, Map.Entry<String, String>> map) {
        var incomplete = new ArrayList<Supplier<Path>>();
        for (var entry : map.entrySet()) {
            String maven = entry.getKey();
            String hash = entry.getValue().getKey();
            String sourceUrl = entry.getValue().getValue();
            String path = "libraries/" + Util.mavenToPath(maven);
            try {
                if (new File(path).exists() && Util.hash(path).equalsIgnoreCase(hash)) {
                    continue;
                }
            } catch (Exception ignored) {
            }
            incomplete.add(new MavenDownloader(Mirrors.getMavenRepo(), maven, path, hash, sourceUrl));
        }
        return incomplete;
    }

    private record MinecraftData(String mirror, String serverUrl, String serverHash) {
    }
}
