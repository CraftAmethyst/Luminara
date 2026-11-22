package org.craftamethyst.luminara.common.mod.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UpdateCheckerTest {

  @Test
  void findsNewestPrereleaseWithAnArtifactForThisLoaderAndMinecraft() {
    var update =
        find(
            "2.0.0-alpha.2",
            "fabric",
            release("Trials", "2.0.0-alpha.10", true, "fabric", "1.20.1"),
            release("FeudalKings", "9.0.0", false, "fabric", "1.20.1"),
            release("Trials", "2.1.0", false, "fabric", "1.21.1"),
            release("Trials", "2.0.1", false, "forge", "1.20.1"),
            release("Trials", "2.0.2", false, "fabric", "1.20.1")
                .replace("\"supported\"", "\"unsupported\""),
            release("Trials", "2.0.0-alpha.3", true, "fabric", "1.20.1"));

    assertEquals(
        new UpdateChecker.Update(
            "2.0.0-alpha.10",
            "https://github.com/CraftAmethyst/Luminara/releases/download/"
                + "Trials/2.0.0-alpha.10/luminara-fabric-1.20.1-2.0.0-alpha.10.jar"),
        update.orElseThrow());
  }

  @Test
  void stableBuildsIgnorePrereleasesAndRecognizeLegacyForgeHotfixes() {
    String hotfix =
        release("Trials", "1.0.15-hotfix", false, "forge", "1.20.1")
            .replace("luminara-forge-", "luminara-");
    assertEquals(
        "1.0.15-hotfix",
        find(
                "1.0.15", "forge", release("Trials", "2.0.0-alpha.1", true, "forge", "1.20.1"),
                hotfix)
            .orElseThrow()
            .version());
    assertTrue(find("1.0.15", "fabric", hotfix).isEmpty());
  }

  @Test
  void finalReleaseIsNewerThanPrereleaseAndEqualVersionsAreIgnored() {
    String stable = release("Trials", "2.0.0", false, "forge", "1.20.1");
    assertEquals("2.0.0", find("2.0.0-rc.1", "forge", stable).orElseThrow().version());
    assertTrue(find("2.0.0+local", "forge", stable).isEmpty());
    assertTrue(find("2.1.0", "forge", stable).isEmpty());
  }

  @Test
  void fetchesSnapshotAndReportsHttpAndSchemaErrors() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/releases",
        exchange -> {
          byte[] body =
              snapshot(release("Trials", "2.0.0", false, "forge", "1.20.1"))
                  .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, body.length);
          try (var response = exchange.getResponseBody()) {
            response.write(body);
          }
        });
    server.start();
    try {
      URI origin = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      assertEquals(
          "2.0.0",
          UpdateChecker.fetch(origin.resolve("/releases"), "1.0.16", "1.20.1", "forge")
              .orElseThrow()
              .version());
      assertThrows(
          IOException.class,
          () -> UpdateChecker.fetch(origin.resolve("/missing"), "1.0.16", "1.20.1", "forge"));
      assertThrows(
          JsonParseException.class,
          () -> UpdateChecker.findUpdate(new StringReader("{}"), "1.0.16", "1.20.1", "forge"));
    } finally {
      server.stop(0);
    }
  }

  private static Optional<UpdateChecker.Update> find(
      String current, String loader, String... releases) {
    return UpdateChecker.findUpdate(new StringReader(snapshot(releases)), current, "1.20.1", loader);
  }

  private static String snapshot(String... releases) {
    return """
        {"schemaVersion": 1, "repo": "CraftAmethyst/Luminara", "releases": [%s]}
        """.formatted(String.join(",", releases));
  }

  private static String release(
      String branch, String version, boolean prerelease, String loader, String minecraft) {
    return """
        {
          "tag": "%s/%s", "branch": "%s", "version": "%s", "prerelease": %s,
          "assets": [
            {"name": "luminara-%s-%s-%s.jar", "loader": "%s", "minecraft": "%s",
             "compatibility": "supported"}
          ]
        }
        """.formatted(
        branch,
        version,
        branch.toLowerCase(Locale.ROOT),
        version,
        prerelease,
        loader,
        minecraft,
        version,
        loader,
        minecraft);
  }
}
