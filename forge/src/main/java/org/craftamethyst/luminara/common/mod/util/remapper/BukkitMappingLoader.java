package org.craftamethyst.luminara.common.mod.util.remapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import net.md_5.specialsource.JarMapping;

final class BukkitMappingLoader {

  private static final String CHUNK_TRACKER_SECTION =
      "net/minecraft/server/level/ChunkMap net/minecraft/server/level/ChunkTracker";

  private BukkitMappingLoader() {}

  static void load(JarMapping mapping, String content) throws IOException {
    var sectionStart = content.indexOf(CHUNK_TRACKER_SECTION);
    if (sectionStart < 0) {
      mapping.loadMappings(new BufferedReader(new StringReader(content)), null, null, true);
      return;
    }

    var nextSection =
        content
            .substring(sectionStart)
            .lines()
            .skip(1)
            .dropWhile(line -> line.startsWith("\t"))
            .findFirst()
            .orElseThrow();
    var sectionEnd = content.indexOf(nextSection);
    mapping.loadMappings(
        new BufferedReader(
            new StringReader(content.substring(0, sectionStart) + content.substring(sectionEnd))),
        null,
        null,
        true);
    mapping.loadMappings(
        new BufferedReader(new StringReader(content.substring(sectionStart, sectionEnd))),
        null,
        null,
        true);
  }
}
