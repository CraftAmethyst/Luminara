package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.ConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class LoggingConfigurator {

    private static final String CONSOLE_CHARSET_PROPERTY = "luminara.console.charset";

    private LoggingConfigurator() {
    }

    static void apply(ConfigSpec spec) {
        try {
            configureConsoleCharset();
            boolean useSimpleFormat;
            try {
                useSimpleFormat =
                    spec != null &&
                        spec.getLogging() != null &&
                        spec.getLogging().isUseSimpleFormat();
            } catch (Exception e) {
                useSimpleFormat = false;
            }

            String configFile = useSimpleFormat
                ? "arclight-log4j2.xml"
                : "arclight-log4j2-detailed.xml";
            reconfigureLogging(configFile);

            System.out.println(
                "[Luminara] Applied logging configuration: " +
                    (useSimpleFormat ? "Simple format" : "Detailed format")
            );
        } catch (Exception e) {
            System.err.println(
                "Failed to apply logging configuration: " + e.getMessage()
            );
        }
    }

    private static void configureConsoleCharset() {
        if (System.getProperty(CONSOLE_CHARSET_PROPERTY) != null) {
            return;
        }
        String charset = null;
        for (String property : new String[]{
            "stdout.encoding", "sun.stdout.encoding", "native.encoding", "file.encoding"
        }) {
            String candidate = System.getProperty(property);
            if (candidate != null && !candidate.isBlank()) {
                try {
                    charset = Charset.forName(candidate).name();
                    break;
                } catch (Exception ignored) {
                }
            }
        }
        System.setProperty(
            CONSOLE_CHARSET_PROPERTY,
            charset == null ? StandardCharsets.UTF_8.name() : charset
        );
    }

    private static void reconfigureLogging(String configFile) {
        try {
            ClassLoader classLoader =
                LoggingConfigurator.class.getClassLoader();
            URI configUri = Objects.requireNonNull(
                classLoader.getResource(configFile)
            ).toURI();

            LoggerContext context = (LoggerContext) LogManager.getContext(
                false
            );
            context.setConfigLocation(configUri);
            context.reconfigure();
        } catch (Exception e) {
            System.err.println(
                "Failed to reconfigure logging: " + e.getMessage()
            );
            System.setProperty("log4j.configurationFile", configFile);
        }
    }
}
