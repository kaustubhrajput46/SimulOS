package com.osshell.scheduling;

/**
 * Thread wrapper for running schedulers in the background.
 */
public class SchedulerThread extends Thread {
    private final ProcessScheduler scheduler;
    private final SchedulerLogger logger;
    private volatile boolean stopped = false;

    public SchedulerThread(ProcessScheduler scheduler, SchedulerLogger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
        this.setDaemon(false); // Keep JVM alive while scheduler runs
        this.setName("SchedulerThread-" + scheduler.getAlgorithmName());
    }

    @Override
    public void run() {
        try {
            scheduler.run();

            // Log final metrics when scheduler is stopped
            // Note: Scheduler now runs indefinitely until stopped
            if (stopped) {
                logger.logMetricsSummary(scheduler.getMetrics());
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.log("ERROR: Scheduler encountered exception: " + e.getMessage());
            }
            e.printStackTrace();
        }
        // Logger is NOT closed here - only closed when stopScheduler() is called
    }

    /**
     * Signals the scheduler to stop gracefully.
     */
    public void stopScheduler() {
        stopped = true;
        scheduler.stop();

        // Wait for thread to finish (with timeout)
        try {
            this.join(2000); // Wait up to 2 seconds
            if (this.isAlive()) {
                if (logger != null) {
                    logger.log("WARNING: Scheduler did not stop gracefully, forcing interrupt");
                }
                this.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Close logger only when stopping
            if (logger != null) {
                logger.close();
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public ProcessScheduler getScheduler() {
        return scheduler;
    }

    public SchedulerLogger getLogger() {
        return logger;
    }
}
