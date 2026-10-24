package io.izzel.arclight.gradle.runnable;

import java.nio.file.Path;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.cadixdev.lorenz.MappingSet;

public class SourceRemapper implements Runnable {

  @Getter @Setter private Path sourceDir;

  @Getter @Setter private Path outputDir;

  @Getter @Setter private MappingSet mappings;

  @Override
  public void run() {
    Objects.requireNonNull(sourceDir);
    Objects.requireNonNull(outputDir);
    Objects.requireNonNull(mappings);
  }
}
