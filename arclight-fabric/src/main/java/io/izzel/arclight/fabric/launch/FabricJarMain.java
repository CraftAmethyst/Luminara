package io.izzel.arclight.fabric.launch;

import java.io.*;

public final class FabricJarMain {

    private static final int MIN_CLASS_VERSION = 61;
    private static final int MIN_JAVA_VERSION = 17;
    private static final String EULA_URL = "https://account.mojang.com/documents/minecraft_eula";
    private static final String EULA_FILE = "eula.txt";

    private FabricJarMain() {
    }

    public static void main(String[] args) throws Throwable {
        int javaVersion = (int) Float.parseFloat(System.getProperty("java.class.version"));
        if (javaVersion < MIN_CLASS_VERSION) {
            System.err.println("Luminara Fabric requires Java " + MIN_JAVA_VERSION);
            System.err.println("Current: " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (!checkEula()) {
            System.err.println("You need to agree to the EULA to run the server.");
            System.exit(-1);
            return;
        }

        Main_Fabric.main(args);
    }

    private static boolean checkEula() throws IOException {
        File eulaFile = new File(EULA_FILE);
        if (eulaFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(eulaFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("eula=")) {
                        String value = line.trim().substring(5);
                        if (value.trim().equalsIgnoreCase("true")) {
                            return true;
                        }
                        System.out.println("EULA is currently set to \"" + value.trim() + "\" and must be accepted.");
                        return promptUserForEula(eulaFile, true);
                    }
                }
            }
        }
        return promptUserForEula(eulaFile, false);
    }

    private static boolean promptUserForEula(File eulaFile, boolean isUpdating) throws IOException {
        System.out.println("By running this server, you agree to the Minecraft EULA.");
        System.out.println("Read the EULA at: " + EULA_URL);
        System.out.print("Do you agree to the EULA? (y/N): ");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input = reader.readLine();
            if (input != null && input.trim().toLowerCase().startsWith("y")) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(eulaFile))) {
                    writer.write("# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\n");
                    writer.write("# Generated via Luminara Fabric launcher\n");
                    writer.write("eula=true\n");
                }
                System.out.println("EULA has been " + (isUpdating ? "updated" : "accepted") + " and saved.");
                return true;
            }
            System.out.println("EULA not accepted. Server will not start.");
            return false;
        }
    }
}
