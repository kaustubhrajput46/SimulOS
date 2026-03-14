package com.osshell.scheduling;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe logger for scheduler execution details.
 */
public class SchedulerLogger {
    private final BufferedWriter writer;
    private final String logFileName;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public SchedulerLogger() throws IOException {
        // Create log file with timestamp
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        this.logFileName = "scheduler-" + timestamp + ".log";
        this.writer = new BufferedWriter(new FileWriter(logFileName, true));
        
        log("=".repeat(80));
        log("Scheduler Log Started");
        log("=".repeat(80));
    }

    /**
     * Log a general message with timestamp.
     */
    public synchronized void log(String message) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            writer.write("[" + timestamp + "] " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }

    /**
     * Log scheduler initialization.
     */
    public void logSchedulerStart(String algorithmName, int timeQuantum) {
        log("");
        log(">>> Scheduler Started: " + algorithmName);
        if (timeQuantum > 0) {
            log("    Time Quantum: " + timeQuantum + "ms");
        }
        log("");
    }

    /**
     * Log when a process is added.
     */
    public void logProcessAdded(SimulatedProcess process) {
        log("Process Added: " + process.getName() + 
            " (ID: P" + process.getProcessId() + 
            ", Burst: " + process.getBurstTime() + "ms" +
            ", Priority: " + process.getPriority() + ")");
    }

    /**
     * Log execution cycle details.
     */
    public void logCycle(int cycle, String processName, int processId, 
                         int remainingBefore, int executionTime, int remainingAfter) {
        log(String.format("Cycle %d: Executing %s (P%d) - Remaining: %dms -> Executing %dms -> Remaining: %dms",
            cycle, processName, processId, remainingBefore, executionTime, remainingAfter));
    }

    /**
     * Log process completion.
     */
    public void logCompletion(String processName, int processId) {
        log(">>> Process " + processName + " (P" + processId + ") COMPLETED");
    }

    /**
     * Log process preemption (detailed).
     */
    public void logPreemption(String preemptedName, int preemptedId, int preemptedPriority,
                              String preemptorName, int preemptorId, int preemptorPriority,
                              int remainingTime) {
        log(">>> PREEMPTION: " + preemptedName + " (P" + preemptedId + 
            ", Priority: " + preemptedPriority + ", Remaining: " + remainingTime + "ms)" +
            " preempted by " + preemptorName + " (P" + preemptorId + 
            ", Priority: " + preemptorPriority + ")");
    }

    /**
     * Log when scheduler stops.
     */
    public void logSchedulerStop(String reason) {
        log("");
        log(">>> Scheduler Stopped: " + reason);
        log("");
    }

    /**
     * Log final metrics summary.
     */
    public void logMetricsSummary(SchedulingMetrics metrics) {
        log("");
        log("=== Final Metrics Summary ===");
        log(String.format("Total Processes Completed: %d", metrics.getCompletedProcesses().size()));
        log(String.format("Average Waiting Time: %.2f ms", metrics.getAverageWaitingTime()));
        log(String.format("Average Turnaround Time: %.2f ms", metrics.getAverageTurnaroundTime()));
        log(String.format("Average Response Time: %.2f ms", metrics.getAverageResponseTime()));
        log("");
        
        for (SimulatedProcess process : metrics.getCompletedProcesses()) {
            log(String.format("  P%d (%s): Burst=%dms, Wait=%dms, Turnaround=%dms, Response=%dms",
                process.getProcessId(),
                process.getName(),
                process.getBurstTime(),
                process.getWaitingTime(),
                process.getTurnaroundTime(),
                process.getResponseTime()));
        }
        log("=".repeat(80));
    }

    /**
     * Close the log file.
     */
    public synchronized void close() {
        try {
            if (writer != null) {
                log("Scheduler Log Ended");
                log("=".repeat(80));
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing log: " + e.getMessage());
        }
    }

    public String getLogFileName() {
        return logFileName;
    }
}
