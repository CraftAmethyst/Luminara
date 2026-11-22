package org.craftamethyst.luminara.i18n;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import ninja.leaping.configurate.ConfigurationNode;

final class I18nCommentInjector {

  private I18nCommentInjector() {}

  static boolean hasLocalizedComments(CommentedConfig config) {
    String comment = commentText(loadComments(config), "_v");
    return comment.isEmpty() || Objects.equals(config.getComment("_v"), comment);
  }

  static void localize(CommentedConfig config) {
    applyComments(config, loadComments(config), "");
  }

  private static ConfigurationNode loadComments(CommentedConfig config) {
    String current = config.getOrElse("locale.current", ArclightLocale.systemLocale());
    String fallback = config.getOrElse("locale.fallback", ArclightLocale.DEFAULT_FALLBACK);
    try {
      return ArclightLocale.readBundle(
              ArclightLocale.hasBundle(current) ? current : fallback)
          .getNode("comments");
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load localized configuration comments", e);
    }
  }

  private static void applyComments(
      CommentedConfig config, ConfigurationNode comments, String prefix) {
    for (var entry : config.valueMap().entrySet()) {
      String key = entry.getKey();
      String path = prefix + key;
      String text = commentText(comments, path);
      if (!text.isEmpty()) {
        config.setComment(List.of(key), text);
      }
      if (entry.getValue() instanceof CommentedConfig section) {
        applyComments(section, comments, path + ".");
      }
    }
  }

  private static String commentText(ConfigurationNode comments, String path) {
    String[] elements = (path + ".comment").split("\\.");
    ConfigurationNode node = comments;
    for (int index = 0; index < elements.length; index++) {
      var child = node.getNode(elements[index]);
      if (child.isVirtual()) {
        String flattened = String.join(".", Arrays.copyOfRange(elements, index, elements.length));
        node = node.getNode(flattened);
        break;
      }
      node = child;
    }
    String text =
        switch (node.getValueType()) {
          case SCALAR -> node.getString("");
          case LIST ->
              node.getChildrenList().stream()
                  .map(line -> line.getString(""))
                  .collect(Collectors.joining("\n"));
          default -> "";
        };
    return text.lines()
        .map(String::strip)
        .filter(line -> !line.isEmpty())
        .map(line -> " " + line)
        .collect(Collectors.joining("\n"));
  }
}
