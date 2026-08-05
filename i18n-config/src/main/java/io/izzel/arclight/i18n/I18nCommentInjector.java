package io.izzel.arclight.i18n;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

final class I18nCommentInjector {

    private I18nCommentInjector() {}

    static String injectComments(String configContent, String currentLocale)
        throws Exception {
        ConfigurationNode commentsNode = loadCommentsNode(currentLocale);
        if (commentsNode == null || commentsNode.isVirtual()) {
            return updateLocaleInContent(configContent, currentLocale);
        }
        String withComments = injectCommentsForContent(
            configContent,
            commentsNode
        );
        return updateLocaleInContent(withComments, currentLocale);
    }

    static boolean hasInjectedComments(
        String configContent,
        String currentLocale
    ) throws Exception {
        ConfigurationNode commentsNode = loadCommentsNode(currentLocale);
        if (commentsNode == null || commentsNode.isVirtual()) return true;

        ConfigurationNode versionComment = findCommentNode(
            commentsNode,
            "_v"
        );
        if (versionComment == null) return true;
        String commentText = getCommentText(versionComment);
        for (String line : commentText.split("\\n")) {
            String trimmed = line.trim();
            if (
                !trimmed.isEmpty() &&
                !configContent.contains("# " + trimmed)
            ) {
                return false;
            }
        }
        return true;
    }

    private static ConfigurationNode loadCommentsNode(String currentLocale)
        throws Exception {
        InputStream i18nStream = I18nCommentInjector.class.getResourceAsStream(
            "/META-INF/i18n/" + currentLocale + ".yml"
        );

        if (i18nStream == null) {
            String fallbackLocale = ArclightLocale.getInstance().fallback();
            i18nStream = I18nCommentInjector.class.getResourceAsStream(
                "/META-INF/i18n/" + fallbackLocale + ".yml"
            );
        }

        if (i18nStream == null) {
            return null;
        }

        final InputStream finalI18nStream = i18nStream;
        YAMLConfigurationLoader i18nLoader = YAMLConfigurationLoader.builder()
            .setSource(() ->
                new BufferedReader(
                    new InputStreamReader(
                        finalI18nStream,
                        StandardCharsets.UTF_8
                    )
                )
            )
            .build();
        ConfigurationNode i18nNode = i18nLoader.load();
        return i18nNode.getNode("comments");
    }

    private static String injectCommentsForContent(
        String configContent,
        ConfigurationNode commentsNode
    ) {
        StringBuilder result = new StringBuilder(configContent.length() + 128);

        String[] lines = configContent.split("\n");
        List<String> currentPath = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                int indentLevel = getIndentLength(line);
                String key = extractKeyFromLine(trimmed);
                if (key != null) {
                    int currentLevel = Math.max(0, indentLevel / 2);

                    while (currentPath.size() > currentLevel) {
                        currentPath.remove(currentPath.size() - 1);
                    }

                    currentPath.add(key);

                    String fullPath = String.join(".", currentPath);

                    ConfigurationNode commentNode = findCommentNode(
                        commentsNode,
                        fullPath
                    );
                    if (
                        commentNode != null &&
                        !commentNode.isVirtual() &&
                        commentNode.getValue() != null
                    ) {
                        String commentText = getCommentText(commentNode);
                        if (!commentText.isEmpty()) {
                            String indent = getIndent(line);
                            String[] commentLines = commentText.split("\n");
                            for (String comment : commentLines) {
                                String commentTrimmed = comment.trim();
                                if (!commentTrimmed.isEmpty()) {
                                    result
                                        .append(indent)
                                        .append("# ")
                                        .append(commentTrimmed)
                                        .append("\n");
                                }
                            }
                        }
                    }
                }
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }

    private static String extractKeyFromLine(String trimmed) {
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        int colonIndex = trimmed.indexOf(':');
        if (colonIndex > 0) {
            return trimmed.substring(0, colonIndex).trim();
        }
        return null;
    }

    private static ConfigurationNode findCommentNode(
        ConfigurationNode commentsNode,
        String fullPath
    ) {
        String[] path = (fullPath + ".comment").split("\\.");
        ConfigurationNode current = commentsNode;
        for (int index = 0; index < path.length; index++) {
            ConfigurationNode child = current
                .getChildrenMap()
                .get(path[index]);
            if (child != null) {
                current = child;
                continue;
            }

            StringBuilder flattenedKey = new StringBuilder();
            for (int suffix = index; suffix < path.length; suffix++) {
                if (flattenedKey.length() > 0) flattenedKey.append('.');
                flattenedKey.append(path[suffix]);
            }
            child = current.getChildrenMap().get(flattenedKey.toString());
            return child;
        }
        return current;
    }

    private static String getCommentText(ConfigurationNode commentNode) {
        Object value = commentNode.getValue();
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            for (Object item : (Iterable<?>) value) {
                if (item instanceof String) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append((String) item);
                }
            }
            return sb.toString();
        }
        return "";
    }

    private static String getIndent(String line) {
        int indentLength = 0;
        while (
            indentLength < line.length() &&
            Character.isWhitespace(line.charAt(indentLength))
        ) {
            indentLength++;
        }
        return line.substring(0, indentLength);
    }

    private static int getIndentLength(String line) {
        int indentLength = 0;
        while (
            indentLength < line.length() &&
            Character.isWhitespace(line.charAt(indentLength))
        ) {
            indentLength++;
        }
        return indentLength;
    }

    private static String updateLocaleInContent(
        String content,
        String currentLocale
    ) {
        if (content.contains("current:")) {
            content = content.replaceAll(
                "(?m)^([\\t ]*current:)\\s*[a-zA-Z_0-9]+$",
                "$1 " + currentLocale
            );
        } else {
            content = content.replaceFirst(
                "(locale:\\s*\\n\\s*fallback:\\s*zh_cn)",
                "$1\n  current: " + currentLocale
            );
        }
        return content;
    }
}
