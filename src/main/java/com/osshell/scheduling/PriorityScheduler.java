package com.osshell.scheduling;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Implements Priority-Based scheduling algorithm with preemption.
 * Lower priority number = higher priority.
 * Thread-safe for background execution and dynamic process addition.
 */
public class PriorityScheduler implements ProcessScheduler {
    private final PriorityQueue<SimulatedProcess> readyQueue;
    private final SchedulingMetrics metrics;
    private SimulatedProcess currentProcess;
    private volatile boolean shouldStop = false;
    private volatile boolean running = false;
    private SchedulerLogger logger;
    private final Object queueLock = new Object();

    public PriorityScheduler() {
        // Priority queue ordered by priority (lower number = higher priority)
        // If priorities are equal, use arrival time (FCFS)
        this.readyQueue = new PriorityQueue<>(Comparator
                .comparingInt(SimulatedProcess::getPriority)
                .thenComparingLong(SimulatedProcess::getArrivalTime));
        this.metrics = new SchedulingMetrics();
        this.currentProcess = null;
    }

    @Override
    public void addProcess(SimulatedProcess process) {
        synchronized (queueLock) {
            readyQueue.offer(process);
        }
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
            logger.logSchedulerStart(getAlgorithmName(), -1);
        }

        int cycleCount = 0;
        final int PREEMPTION_CHECK_INTERVAL = 10; // Check for higher priority every 10ms

        SimulatedProcess nextProcess;
        while (!shouldStop) {
            synchronized (queueLock) {
                nextProcess = readyQueue.poll();
            }

            if (nextProcess == null) {
                // Wait for new processes to be added
                try {
                    Thread.sleep(50); // Check every 50ms for new processes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue; // Check queue again
            }

            currentProcess = nextProcess;
            cycleCount++;

            // Log when starting a new process
            if (logger != null) {
                logger.log("Starting execution of " + currentProcess.getName() +
                          " (P" + currentProcess.getProcessId() +
                          ", Priority: " + currentProcess.getPriority() +
                          ", Burst: " + currentProcess.getBurstTime() + "ms)");
            }

            int executionSlices = 0; // Track number of time slices for this process

            // Execute in small time slices to allow preemption
            while (!shouldStop && currentProcess.getRemainingTime() > 0) {
                SimulatedProcess higherPriorityProcess;
                synchronized (queueLock) {
                    higherPriorityProcess = readyQueue.peek();
                }

                // Check if there's a higher priority process waiting
                if (higherPriorityProcess != null &&
                        higherPriorityProcess.getPriority() < currentProcess.getPriority()) {

                    // Log preemption - ALWAYS log this important event
                    if (logger != null) {
                        logger.logPreemption(
                                currentProcess.getName(),
                                currentProcess.getProcessId(),
                                currentProcess.getPriority(),
                                higherPriorityProcess.getName(),
                                higherPriorityProcess.getProcessId(),
                                higherPriorityProcess.getPriority(),
                                currentProcess.getRemainingTime()
                        );
                    }

                    // Put current process back in queue
                    synchronized (queueLock) {
                        readyQueue.offer(currentProcess);
                    }
                    break;
                }

                // Execute for a small time slice
                int timeSlice = Math.min(PREEMPTION_CHECK_INTERVAL, currentProcess.getRemainingTime());
                int remainingBefore = currentProcess.getRemainingTime();

                currentProcess.execute(timeSlice);
                executionSlices++;

                // Log execution periodically (every 100ms worth of execution)
                if (logger != null && executionSlices % 10 == 1) {
                    logger.logCycle(cycleCount, currentProcess.getName(), currentProcess.getProcessId(),
                            remainingBefore, timeSlice, currentProcess.getRemainingTime());
                }

                if (currentProcess.isCompleted()) {
                    if (logger != null) {
                        logger.logCompletion(currentProcess.getName(), currentProcess.getProcessId());
                    }
                    metrics.addCompletedProcess(currentProcess);
                    break;
                }
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
        return "Priority-Based (Preemptive)";
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
        synchronized (queueLock) {
            return readyQueue.size();
        }
    }
}
