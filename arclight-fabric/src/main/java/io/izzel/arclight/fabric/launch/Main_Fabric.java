package io.izzel.arclight.fabric.launch;

import io.izzel.arclight.forgeinstaller.FabricInstaller;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class Main_Fabric {

    private Main_Fabric() {
    }

    public static void main(String[] args) throws Throwable {
        try {
            var install = FabricInstaller.applicationInstall();
            var self = Paths.get(Main_Fabric.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();

            URL[] urls = Stream.concat(Stream.of(self), install.getValue().stream())
                    .map(Main_Fabric::toUrl)
                    .toArray(URL[]::new);

            var classLoader = new URLClassLoader(urls, ClassLoader.getPlatformClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
            System.setProperty("fabric.addMods", self.toString());
            var mainClass = Class.forName(install.getKey(), false, classLoader);
            var main = MethodHandles.lookup().findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class));
            main.invoke((Object) args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("Fail to launch Luminara Fabric server.");
            System.exit(-1);
        }
    }

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
