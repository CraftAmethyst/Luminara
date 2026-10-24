package io.izzel.arclight.boot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmbeddedJarExtractorTest {

  @TempDir Path directory;

  @Test
  void replacesChangedContentWithTheSameVersionName() throws Exception {
    Path target = directory.resolve("same-version.jar");
    Files.write(target, new byte[] {1, 2, 3});
    Files.write(directory.resolve("old-version.jar"), new byte[] {4});

    EmbeddedJarExtractor.extract(
        new ByteArrayInputStream(new byte[] {5, 6, 7}), directory, "same-version.jar", false);

    assertArrayEquals(new byte[] {5, 6, 7}, Files.readAllBytes(target));
    assertFalse(Files.exists(directory.resolve("old-version.jar")));
  }
}
