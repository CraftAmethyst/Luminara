package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.ConfigSpec;
import java.net.URI;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

final class LoggingConfigurator {

    private LoggingConfigurator() {}

    static void apply(ConfigSpec spec) {
        try {
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
