package com.osshell.scheduling;

/**
 * Interface for process scheduling algorithms.
 */
public interface ProcessScheduler {
    /**
     * Adds a process to the scheduler's queue.
     */
    void addProcess(SimulatedProcess process);

    /**
     * Runs the scheduling algorithm until all processes complete.
     */
    void run();

    /**
     * Gets the scheduling metrics.
     */
    SchedulingMetrics getMetrics();

    /**
     * Gets the name of the scheduling algorithm.
     */
    String getAlgorithmName();

    /**
     * Signals the scheduler to stop execution gracefully.
     */
    void stop();

    /**
     * Checks if the scheduler is currently running.
     */
    boolean isRunning();

    /**
     * Gets the number of processes waiting in the queue.
     */
    int getQueueSize();

    /**
     * Sets the logger for this scheduler.
     */
    void setLogger(SchedulerLogger logger);
}
