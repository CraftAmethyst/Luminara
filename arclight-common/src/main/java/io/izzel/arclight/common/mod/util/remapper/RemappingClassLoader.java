package io.izzel.arclight.common.mod.util.remapper;

public interface RemappingClassLoader {

  ClassLoaderRemapper getRemapper();

  ArclightRemapConfig getRemapConfig();
}
