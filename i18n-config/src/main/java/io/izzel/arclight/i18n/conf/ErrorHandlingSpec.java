package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ErrorHandlingSpec {
    @Setting("continue-on-crash")
    private boolean continueOnCrash;

    @Setting("crash-report-directory")
    private String crashReportDirectory = "crash-reports";

    public boolean isContinueOnCrash() {
        return continueOnCrash;
    }

    public String getCrashReportDirectory() {
        return crashReportDirectory == null || crashReportDirectory.isBlank() ? "crash-reports" : crashReportDirectory;
    }
}
