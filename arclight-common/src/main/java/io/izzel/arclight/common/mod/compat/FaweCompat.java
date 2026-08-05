package io.izzel.arclight.common.mod.compat;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FaweCompat {

    private static final Pattern MC_VERSION_PATTERN = Pattern.compile(
        "(\\d+)\\.(\\d+)(?:\\.(\\d+))?"
    );
    private static final Pattern FAWE_VERSIONED_ADAPTER_PATTERN =
        Pattern.compile(".*\\.v\\d+_\\d+_R\\d+\\.PaperweightFaweAdapter$");
    private static final String FAWE_IMPL_PREFIX =
        "com.sk89q.worldedit.bukkit.adapter.impl.fawe.";

    private FaweCompat() {
    }

    public static String getCraftBukkitPackageVersion() {
        String fullPackagePath = Bukkit.getServer()
            .getClass()
            .getPackage()
            .getName();
        String packageVersion = fullPackagePath.substring(
            fullPackagePath.lastIndexOf('.') + 1
        );
        if (!"v".equals(packageVersion)) {
            return packageVersion;
        }

        String bukkitVersion = Bukkit.getBukkitVersion();
        Matcher matcher = MC_VERSION_PATTERN.matcher(bukkitVersion);
        if (matcher.find()) {
            String major = matcher.group(1);
            String minor = matcher.group(2);
            String release = matcher.group(3) == null ? "1" : matcher.group(3);
            return "v" + major + "_" + minor + "_R" + release;
        }

        return "v1_20_R1";
    }

    public static boolean addFilteredFaweCandidate(
        List list,
        Object candidate
    ) {
        if (shouldIncludeFaweCandidate(candidate)) {
            return list.add(candidate);
        }
        return false;
    }

    public static void addFilteredFaweCandidateIndexed(
        List list,
        int index,
        Object candidate
    ) {
        if (shouldIncludeFaweCandidate(candidate)) {
            list.add(index, candidate);
        }
    }

    private static boolean shouldIncludeFaweCandidate(Object candidate) {
        if (!(candidate instanceof String className)) {
            return true;
        }
        if (!className.startsWith(FAWE_IMPL_PREFIX)) {
            return true;
        }
        if (!className.endsWith(".PaperweightFaweAdapter")) {
            return false;
        }
        if (className.indexOf('$') >= 0) {
            return false;
        }

        String targetMarker =
            "." + getCraftBukkitPackageVersion() + ".PaperweightFaweAdapter";
        if (className.endsWith(targetMarker)) {
            return true;
        }

        return !FAWE_VERSIONED_ADAPTER_PATTERN.matcher(className).matches();
    }
}
