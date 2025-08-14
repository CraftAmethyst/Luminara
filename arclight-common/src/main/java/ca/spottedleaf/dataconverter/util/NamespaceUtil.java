package ca.spottedleaf.dataconverter.util;

public final class NamespaceUtil {

    public static String correctNamespace(final String value) {
        if (value == null) {
            return null;
        }
        final int colonIndex = value.indexOf(':');
        if (colonIndex < 0) {
            return "minecraft:" + value;
        }

        return value;
    }

    public static String correctNamespaceOrNull(final String value) {
        if (value == null) {
            return null;
        }
        final int colonIndex = value.indexOf(':');
        if (colonIndex < 0) {
            return "minecraft:" + value;
        }

        return value;
    }

    public static String getNamespace(final String value) {
        if (value == null) {
            return null;
        }
        final int colonIndex = value.indexOf(':');
        if (colonIndex < 0) {
            return "minecraft";
        }

        return value.substring(0, colonIndex);
    }

    public static String getPath(final String value) {
        if (value == null) {
            return null;
        }
        final int colonIndex = value.indexOf(':');
        if (colonIndex < 0) {
            return value;
        }

        return value.substring(colonIndex + 1);
    }

    private NamespaceUtil() {}

}
