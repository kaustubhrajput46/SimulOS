package com.osshell.scheduling;

/**
 * Represents a simulated process for scheduling algorithms.
 */
public class SimulatedProcess {
    private final int processId;
    private final String name;
    private final int burstTime; // Total execution time needed
    private int remainingTime; // Time remaining for execution
    private final int priority; // Lower number = higher priority
    private final long arrivalTime; // When the process arrived
    private long startTime; // When the process first started executing
    private long completionTime; // When the process completed
    private ProcessState state;
    private int executionCount; // Number of times process has been scheduled

    public SimulatedProcess(int processId, String name, int burstTime, int priority) {
        this.processId = processId;
        this.name = name;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.arrivalTime = System.currentTimeMillis();
        this.state = ProcessState.READY;
        this.executionCount = 0;
        this.startTime = -1;
        this.completionTime = -1;
    }

    public void execute(int timeSlice) {
        if (state == ProcessState.READY || state == ProcessState.RUNNING) {
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            }
            
            state = ProcessState.RUNNING;
            executionCount++;
            
            int executionTime = Math.min(timeSlice, remainingTime);
            
            // Simulate process execution
            try {
                Thread.sleep(executionTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            remainingTime -= executionTime;
            
            if (remainingTime <= 0) {
                state = ProcessState.TERMINATED;
                completionTime = System.currentTimeMillis();
            } else {
                state = ProcessState.READY;
            }
        }
    }

    public boolean isCompleted() {
        return state == ProcessState.TERMINATED;
    }

    public int getProcessId() {
        return processId;
    }

    public String getName() {
        return name;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public long getWaitingTime() {
        if (completionTime == -1) {
            return 0;
        }
        return completionTime - arrivalTime - burstTime;
    }

    public long getTurnaroundTime() {
        if (completionTime == -1) {
            return 0;
        }
        return completionTime - arrivalTime;
    }

    public long getResponseTime() {
        if (startTime == -1) {
            return 0;
        }
        return startTime - arrivalTime;
    }

    @Override
    public String toString() {
        return String.format("P%d(%s) - Burst: %dms, Priority: %d, Remaining: %dms, State: %s",
                processId, name, burstTime, priority, remainingTime, state);
    }

    public enum ProcessState {
        READY,
        RUNNING,
        WAITING,
        TERMINATED
    }
}
