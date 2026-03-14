package com.osshell.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks and calculates scheduling performance metrics.
 * Thread-safe for concurrent access.
 */
public class SchedulingMetrics {
    private final List<SimulatedProcess> completedProcesses;

    public SchedulingMetrics() {
        this.completedProcesses = new CopyOnWriteArrayList<>();
    }

    public synchronized void addCompletedProcess(SimulatedProcess process) {
        completedProcesses.add(process);
    }

    public void clear() {
        completedProcesses.clear();
    }

    public double getAverageWaitingTime() {
        if (completedProcesses.isEmpty()) {
            return 0.0;
        }
        return completedProcesses.stream()
                .mapToLong(SimulatedProcess::getWaitingTime)
                .average()
                .orElse(0.0);
    }

    public double getAverageTurnaroundTime() {
        if (completedProcesses.isEmpty()) {
            return 0.0;
        }
        return completedProcesses.stream()
                .mapToLong(SimulatedProcess::getTurnaroundTime)
                .average()
                .orElse(0.0);
    }

    public double getAverageResponseTime() {
        if (completedProcesses.isEmpty()) {
            return 0.0;
        }
        return completedProcesses.stream()
                .mapToLong(SimulatedProcess::getResponseTime)
                .average()
                .orElse(0.0);
    }

    public void printMetrics() {
        System.out.println("\n=== Scheduling Performance Metrics ===");
        System.out.println("Total Processes Completed: " + completedProcesses.size());
        System.out.printf("Average Waiting Time: %.2f ms\n", getAverageWaitingTime());
        System.out.printf("Average Turnaround Time: %.2f ms\n", getAverageTurnaroundTime());
        System.out.printf("Average Response Time: %.2f ms\n", getAverageResponseTime());
        
        System.out.println("\n=== Individual Process Metrics ===");
        System.out.println(String.format("%-10s %-15s %-12s %-15s %-15s", 
                "Process", "Burst Time", "Wait Time", "Turnaround", "Response"));
        System.out.println("-".repeat(70));
        
        for (SimulatedProcess process : completedProcesses) {
            System.out.println(String.format("%-10s %-15d %-12d %-15d %-15d",
                    "P" + process.getProcessId(),
                    process.getBurstTime(),
                    process.getWaitingTime(),
                    process.getTurnaroundTime(),
                    process.getResponseTime()));
        }
        System.out.println();
    }

    public List<SimulatedProcess> getCompletedProcesses() {
        return new ArrayList<>(completedProcesses);
    }
}
