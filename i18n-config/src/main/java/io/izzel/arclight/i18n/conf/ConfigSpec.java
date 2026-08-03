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

    @Setting("async-world-save")
    private AsyncWorldSaveSpec asyncWorldSaveSpec;

    @Setting("velocity")
    private VelocitySpec velocitySpec;

    @Setting("error-handling")
    private ErrorHandlingSpec errorHandlingSpec;

    @Setting("logging")
    private LoggingSpec loggingSpec;

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

    public AsyncWorldSaveSpec getAsyncWorldSave() {
        return asyncWorldSaveSpec != null ? asyncWorldSaveSpec : new AsyncWorldSaveSpec();
    }

    public VelocitySpec getVelocity() {
        return velocitySpec;
    }

    public ErrorHandlingSpec getErrorHandling() {
        return errorHandlingSpec;
    }

    public LoggingSpec getLogging() {
        return loggingSpec;
    }
}
