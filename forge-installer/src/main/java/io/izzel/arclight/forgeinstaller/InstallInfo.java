package io.izzel.arclight.forgeinstaller;

import java.util.Map;
import java.util.List;

public class InstallInfo {

    public Installer installer;
    public Map<String, String> libraries;
    public List<String> runtimeLibraries;

    public static class Installer {

        public String minecraft;
        public String forge;
        public String hash;
    }
}
