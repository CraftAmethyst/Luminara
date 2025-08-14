package ca.spottedleaf.concurrentutil.scheduler;

import ca.spottedleaf.concurrentutil.util.Validate;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Scheduled task implementation for Paper's concurrent utilities.
 * This class represents a task that can be scheduled for execution.
 * <p>
 * This implementation is from Paper patch 0007 (ConcurrentUtil.patch).
 */
public final class ScheduledTask implements Runnable {

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private final long id;
    private final Runnable task;
    private final long scheduledTime;
    private final long period; // -1 for non-repeating tasks
    private final AtomicReference<TaskState> state = new AtomicReference<>(TaskState.IDLE);
    private volatile Consumer<ScheduledTask> onComplete;

    public ScheduledTask(final Runnable task, final long scheduledTime) {
        this(task, scheduledTime, -1L);
    }

    public ScheduledTask(final Runnable task, final long scheduledTime, final long period) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.task = Validate.notNull(task, "Task cannot be null");
        this.scheduledTime = scheduledTime;
        this.period = period;
    }

    /**
     * Creates a new scheduled task.
     */
    public static ScheduledTask create(final Runnable task, final long scheduledTime) {
        return new ScheduledTask(task, scheduledTime);
    }

    /**
     * Creates a new repeating scheduled task.
     */
    public static ScheduledTask createRepeating(final Runnable task, final long scheduledTime, final long period) {
        return new ScheduledTask(task, scheduledTime, period);
    }

    /**
     * Gets the unique ID of this task.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Gets the scheduled execution time.
     */
    public long getScheduledTime() {
        return this.scheduledTime;
    }

    /**
     * Gets the period for repeating tasks.
     *
     * @return The period in nanoseconds, or -1 for non-repeating tasks
     */
    public long getPeriod() {
        return this.period;
    }

    /**
     * Checks if this is a repeating task.
     */
    public boolean isRepeating() {
        return this.period > 0;
    }

    /**
     * Gets the current state of this task.
     */
    public TaskState getState() {
        return this.state.get();
    }

    /**
     * Sets the completion callback.
     */
    public void setOnComplete(final Consumer<ScheduledTask> onComplete) {
        this.onComplete = onComplete;
    }

    /**
     * Cancels this task.
     *
     * @return true if the task was cancelled, false if it was already completed or cancelled
     */
    public boolean cancel() {
        return this.state.compareAndSet(TaskState.IDLE, TaskState.CANCELLED) ||
                this.state.compareAndSet(TaskState.SCHEDULED, TaskState.CANCELLED);
    }

    /**
     * Checks if this task is cancelled.
     */
    public boolean isCancelled() {
        return this.state.get() == TaskState.CANCELLED;
    }

    /**
     * Checks if this task is completed.
     */
    public boolean isCompleted() {
        final TaskState currentState = this.state.get();
        return currentState == TaskState.COMPLETED || currentState == TaskState.CANCELLED;
    }

    /**
     * Marks this task as scheduled.
     */
    public boolean markScheduled() {
        return this.state.compareAndSet(TaskState.IDLE, TaskState.SCHEDULED);
    }

    /**
     * Marks this task as running.
     */
    public boolean markRunning() {
        return this.state.compareAndSet(TaskState.SCHEDULED, TaskState.RUNNING);
    }

    @Override
    public void run() {
        if (!this.markRunning()) {
            return; // Task was cancelled or already running
        }

        try {
            this.task.run();
        } catch (final Throwable throwable) {
            System.err.println("Exception in scheduled task " + this.id + ": " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            this.state.set(TaskState.COMPLETED);

            final Consumer<ScheduledTask> onComplete = this.onComplete;
            if (onComplete != null) {
                try {
                    onComplete.accept(this);
                } catch (final Throwable throwable) {
                    System.err.println("Exception in task completion callback: " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ScheduledTask{" +
                "id=" + this.id +
                ", scheduledTime=" + this.scheduledTime +
                ", period=" + this.period +
                ", state=" + this.state.get() +
                '}';
    }

    /**
     * Represents the state of a scheduled task.
     */
    public enum TaskState {
        IDLE,
        SCHEDULED,
        RUNNING,
        COMPLETED,
        CANCELLED
    }
}
