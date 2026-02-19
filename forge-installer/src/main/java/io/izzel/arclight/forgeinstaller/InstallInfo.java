package io.izzel.arclight.forgeinstaller;

import java.util.HashMap;
import java.util.Map;

public class InstallInfo {

    public Installer installer;
    public Map<String, String> libraries;
    public Map<String, String> fabricExtra;

    public Map<String, String> fabricDeps() {
        var map = new HashMap<String, String>();
        if (this.libraries != null) {
            map.putAll(this.libraries);
        }
        if (this.fabricExtra != null) {
            map.putAll(this.fabricExtra);
        }
        return map;
    }

    public static class Installer {

        public String minecraft;
        public String forge;
        public String hash;
        public String fabricLoader;
        public String fabricLoaderHash;
    }
}
