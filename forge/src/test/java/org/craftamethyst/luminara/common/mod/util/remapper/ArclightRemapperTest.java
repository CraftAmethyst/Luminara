package org.craftamethyst.luminara.common.mod.util.remapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.md_5.specialsource.JarMapping;
import org.junit.jupiter.api.Test;

class ArclightRemapperTest {

  @Test
  void loadsWholeMappingWhenSpecialSourceWorkaroundEntryIsAbsent() throws Exception {
    JarMapping mapping = new JarMapping();

    BukkitMappingLoader.load(
        mapping, "CL: legacy/Plugin current/Plugin\nFD: legacy/Plugin/name current/Plugin/name\n");

    assertEquals("legacy/Plugin", mapping.classes.get("current/Plugin"));
  }

  @Test
  void loadsTheGeneratedRuntimeMappingResource() throws Exception {
    JarMapping mapping = new JarMapping();
    InputStream resource = getClass().getResourceAsStream("/bukkit_srg.srg");
    assertNotNull(resource, "generated Bukkit mappings must be on the Forge runtime classpath");

    String content;
    try (resource) {
      content = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
    }
    BukkitMappingLoader.load(mapping, content);

    assertEquals(
        "net/minecraft/server/v1_14_R1/PlayerChunkMap",
        mapping.classes.get("net/minecraft/world/server/ChunkManager"));
  }
}
