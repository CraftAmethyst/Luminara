package ca.spottedleaf.concurrentutil.executor.standard;

public interface PrioritisedExecutor {

    enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        BLOCKING
    }
}
