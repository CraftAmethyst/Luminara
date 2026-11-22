package org.craftamethyst.luminara.common.mod.update;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;
import org.craftamethyst.luminara.i18n.LuminaraVersion;

public final class UpdateChecker {

  private static final URI SNAPSHOT =
      URI.create(
          "https://raw.githubusercontent.com/CraftAmethyst/luminara-dl-site/main/src/data/releases.json");
  private static final String REPO = "CraftAmethyst/Luminara";
  private static final String DOWNLOAD_BASE = "https://github.com/" + REPO + "/releases/download/";
  private static final int TIMEOUT_MILLIS = 5_000;
  private static final Pattern VERSION =
      Pattern.compile("[0-9]+(?:\\.[0-9]+)+(?:[.-][0-9A-Za-z]+)*(?:\\+[0-9A-Za-z.-]+)?");
  private static final Pattern PRERELEASE =
      Pattern.compile(
          "[.-](?:alpha|beta|rc|pre(?:view)?|snapshot|dev)(?=[.-]|[0-9]|$)",
          Pattern.CASE_INSENSITIVE);
  private static final Gson GSON = new Gson();

  private UpdateChecker() {}

  public static void checkOnStartup(String loader) {
    String currentVersion = LuminaraVersion.version();
    String minecraft = LuminaraVersion.minecraftVersion();
    Thread worker =
        new Thread(
            () -> {
              try {
                fetch(SNAPSHOT, currentVersion, minecraft, loader)
                    .ifPresent(
                        update ->
                            LuminaraLog.LOGGER.info(
                                "update.available",
                                currentVersion,
                                update.version(),
                                update.downloadUrl()));
              } catch (IOException | JsonParseException e) {
                LuminaraLog.LOGGER.warn("update.failed", e.getMessage());
                LuminaraLog.LOGGER.debug("Luminara update check failed", e);
              }
            },
            "Luminara Update Checker");
    worker.setDaemon(true);
    worker.start();
  }

  static Optional<Update> fetch(URI source, String currentVersion, String minecraft, String loader)
      throws IOException {
    var connection = (HttpURLConnection) source.toURL().openConnection();
    connection.setConnectTimeout(TIMEOUT_MILLIS);
    connection.setReadTimeout(TIMEOUT_MILLIS);
    connection.setInstanceFollowRedirects(false);
    try {
      int status = connection.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new IOException("HTTP " + status + " from " + source.getHost());
      }
      try (var reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
        return findUpdate(reader, currentVersion, minecraft, loader);
      }
    } finally {
      connection.disconnect();
    }
  }

  static Optional<Update> findUpdate(
      Reader reader, String currentVersion, String minecraft, String loader) {
    Snapshot snapshot = GSON.fromJson(reader, Snapshot.class);
    if (snapshot == null
        || snapshot.schemaVersion != 1
        || !REPO.equals(snapshot.repo)
        || snapshot.releases == null) {
      throw new JsonParseException("Invalid Luminara release snapshot");
    }

    String installed = withoutBuildMetadata(currentVersion);
    boolean includePrereleases = PRERELEASE.matcher(installed).find();
    var newestVersion = new ComparableVersion(installed);
    Update newest = null;
    String expectedPrefix = "luminara-" + loader + "-" + minecraft + "-";
    String legacyPrefix = "luminara-" + minecraft + "-";
    for (Release release : snapshot.releases) {
      if (release == null) {
        throw new JsonParseException("Missing release in the Luminara release snapshot");
      }
      if (!"trials".equals(release.branch)) {
        continue;
      }
      String version = release.version;
      if (version == null
          || !VERSION.matcher(version).matches()
          || release.prerelease == null
          || release.assets == null
          || !(version.equals(release.tag) || ("Trials/" + version).equals(release.tag))) {
        throw new JsonParseException("Invalid Trials release in the Luminara release snapshot");
      }
      String comparable = withoutBuildMetadata(version);
      if (!includePrereleases && (release.prerelease || PRERELEASE.matcher(comparable).find())) {
        continue;
      }
      var candidate = new ComparableVersion(comparable);
      if (candidate.compareTo(newestVersion) <= 0) {
        continue;
      }
      String expectedName = expectedPrefix + version + ".jar";
      String legacyName = legacyPrefix + version + ".jar";
      for (Asset asset : release.assets) {
        if (asset == null) {
          throw new JsonParseException("Missing asset for " + release.tag);
        }
        if ("supported".equals(asset.compatibility)
            && loader.equals(asset.loader)
            && minecraft.equals(asset.minecraft)
            && (expectedName.equalsIgnoreCase(asset.name)
                || ("forge".equals(loader) && legacyName.equalsIgnoreCase(asset.name)))) {
          String downloadUrl = DOWNLOAD_BASE + release.tag + "/" + asset.name;
          newest = new Update(version, downloadUrl);
          newestVersion = candidate;
          break;
        }
      }
    }
    return Optional.ofNullable(newest);
  }

  private static String withoutBuildMetadata(String version) {
    int separator = version.indexOf('+');
    return separator < 0 ? version : version.substring(0, separator);
  }

  record Update(String version, String downloadUrl) {}

  // Older Gson versions populate fields reflectively and cannot deserialize records.
  private static final class Snapshot {
    int schemaVersion;
    String repo;
    List<Release> releases;
  }

  private static final class Release {
    String tag;
    String branch;
    String version;
    Boolean prerelease;
    List<Asset> assets;
  }

  private static final class Asset {
    String name;
    String loader;
    String minecraft;
    String compatibility;
  }
}
