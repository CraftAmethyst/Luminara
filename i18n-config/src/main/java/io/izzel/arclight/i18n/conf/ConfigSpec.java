package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConfigSpec {

    @Setting("_v")
    private int version;

    @Setting("optimization")
    private OptimizationSpec optimizationSpec;

    @Setting("locale")
    private LocaleSpec localeSpec;

    @Setting("compatibility")
    private CompatSpec compatSpec;

    @Setting("async-catcher")
    private AsyncCatcherSpec asyncCatcherSpec;

    @Setting("velocity")
    private VelocitySpec velocitySpec;

    @Setting("experimental")
    private ExperimentalSpec experimentalSpec;

    @Setting("logging")
    private LoggingSpec loggingSpec;

    @Setting("error-handling")
    private ErrorHandlingSpec errorHandlingSpec;

    public int getVersion() {
        return version;
    }

    public OptimizationSpec getOptimization() {
        return optimizationSpec;
    }

    public LocaleSpec getLocale() {
        return localeSpec;
    }

    public CompatSpec getCompat() {
        return compatSpec;
    }

    public AsyncCatcherSpec getAsyncCatcher() {
        return asyncCatcherSpec;
    }

    public VelocitySpec getVelocity() {
        return velocitySpec;
    }

    public ExperimentalSpec getExperimental() {
        return experimentalSpec;
    }

    public LoggingSpec getLogging() {
        return loggingSpec == null ? new LoggingSpec() : loggingSpec;
    }

    public ErrorHandlingSpec getErrorHandling() {
        return errorHandlingSpec == null ? new ErrorHandlingSpec() : errorHandlingSpec;
    }
}
