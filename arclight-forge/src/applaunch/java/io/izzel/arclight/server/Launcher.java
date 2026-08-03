package io.izzel.arclight.server;

import io.izzel.arclight.boot.application.Main_Forge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Launcher {

    private static final int MIN_CLASS_VERSION = 61;
    private static final int MIN_JAVA_VERSION = 17;
    private static final String EULA_URL = "https://aka.ms/MinecraftEULA";
    private static final String EULA_FILE = "eula.txt";

    public static void main(String[] args) throws Throwable {
        int javaVersion = (int) Float.parseFloat(System.getProperty("java.class.version"));
        if (javaVersion < MIN_CLASS_VERSION) {
            System.err.println("Arclight requires Java " + MIN_JAVA_VERSION);
            System.err.println("Current: " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (!checkEula(Paths.get(EULA_FILE))) {
            System.err.println("Minecraft EULA not accepted. Read " + EULA_URL + " and set eula=true in " + EULA_FILE + ".");
            System.exit(1);
            return;
        }

        Main_Forge.main(args);
    }

    static boolean checkEula(Path eulaFile) throws IOException {
        if (!Files.isRegularFile(eulaFile)) {
            return false;
        }
        List<String> lines = Files.readAllLines(eulaFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if ("eula=true".equals(line)) {
                return true;
            }
        }
        return false;
    }
}
