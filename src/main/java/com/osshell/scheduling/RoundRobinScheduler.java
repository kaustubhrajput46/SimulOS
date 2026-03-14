package com.osshell.scheduling;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implements Round-Robin scheduling algorithm with configurable time quantum.
 * Thread-safe for background execution and dynamic process addition.
 */
public class RoundRobinScheduler implements ProcessScheduler {
    private final ConcurrentLinkedQueue<SimulatedProcess> readyQueue;
    private final SchedulingMetrics metrics;
    private final int timeQuantum;
    private volatile boolean shouldStop = false;
    private volatile boolean running = false;
    private SchedulerLogger logger;

    public RoundRobinScheduler(int timeQuantum) {
        this.readyQueue = new ConcurrentLinkedQueue<>();
        this.metrics = new SchedulingMetrics();
        this.timeQuantum = timeQuantum;
    }

    @Override
    public synchronized void addProcess(SimulatedProcess process) {
        readyQueue.offer(process);
        System.out.println("Added process: " + process);
        if (logger != null) {
            logger.logProcessAdded(process);
        }
    }

    @Override
    public void setLogger(SchedulerLogger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        running = true;
        shouldStop = false;

        if (logger != null) {
            logger.logSchedulerStart(getAlgorithmName(), timeQuantum);
        }

        int cycleCount = 0;

        while (!shouldStop) {
            SimulatedProcess currentProcess = readyQueue.poll();

            if (currentProcess == null) {
                // Wait for new processes to be added
                try {
                    Thread.sleep(50); // Check every 50ms for new processes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            cycleCount++;
            int remainingBefore = currentProcess.getRemainingTime();

            // Always log execution cycles for Round-Robin
            if (logger != null) {
                logger.logCycle(cycleCount, currentProcess.getName(), currentProcess.getProcessId(),
                        remainingBefore, timeQuantum, Math.max(0, remainingBefore - timeQuantum));
            }

            // Execute process for one time quantum
            currentProcess.execute(timeQuantum);

            if (currentProcess.isCompleted()) {
                if (logger != null) {
                    logger.logCompletion(currentProcess.getName(), currentProcess.getProcessId());
                }
                metrics.addCompletedProcess(currentProcess);
            } else {
                // Process preempted by time quantum - add back to queue
                if (logger != null) {
                    logger.log("Process " + currentProcess.getName() + " (P" + currentProcess.getProcessId() +
                              ") preempted by time quantum (Remaining: " + currentProcess.getRemainingTime() + "ms)");
                }
                readyQueue.offer(currentProcess);
            }
        }

        running = false;

        if (shouldStop && logger != null) {
            logger.logSchedulerStop("Stopped by user request");
        }
    }

    @Override
    public SchedulingMetrics getMetrics() {
        return metrics;
    }

    @Override
    public String getAlgorithmName() {
        return "Round-Robin (Quantum: " + timeQuantum + "ms)";
    }

    @Override
    public void stop() {
        shouldStop = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getQueueSize() {
        return readyQueue.size();
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }
}
