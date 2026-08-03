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

    @Setting("error-handling")
    private ErrorHandlingSpec errorHandlingSpec;

    @Setting("logging")
    private LoggingSpec loggingSpec;

    public int getVersion() {
        return version;
    }

    public OptimizationSpec getOptimization() {
        return optimizationSpec != null ? optimizationSpec : new OptimizationSpec();
    }

    public LocaleSpec getLocale() {
        return localeSpec != null ? localeSpec : new LocaleSpec();
    }

    public CompatSpec getCompat() {
        return compatSpec != null ? compatSpec : new CompatSpec();
    }

    public AsyncCatcherSpec getAsyncCatcher() {
        return asyncCatcherSpec != null ? asyncCatcherSpec : new AsyncCatcherSpec();
    }


    public VelocitySpec getVelocity() {
        return velocitySpec != null ? velocitySpec : new VelocitySpec();
    }

    public ErrorHandlingSpec getErrorHandling() {
        return errorHandlingSpec != null ? errorHandlingSpec : new ErrorHandlingSpec();
    }

    public LoggingSpec getLogging() {
        return loggingSpec != null ? loggingSpec : new LoggingSpec();
    }
}
