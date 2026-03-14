package com.osshell.scheduling;

import com.osshell.commands.Command;

import java.io.IOException;
import java.util.Arrays;

/**
 * Implements scheduling commands for Round-Robin and Priority-Based scheduling.
 * Supports background execution and dynamic process addition.
 */
public class SchedulingCommands implements Command {
    private ProcessScheduler currentScheduler;
    private SchedulerThread schedulerThread;
    private int processIdCounter = 1;

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "schedule-rr":
                return scheduleRoundRobin(cmdArgs);
            case "schedule-priority":
                return schedulePriority(cmdArgs);
            case "add-process":
                return addProcess(cmdArgs);
            case "run-scheduler":
                return runScheduler(cmdArgs);
            case "stop-scheduler":
                return stopScheduler(cmdArgs);
            case "show-metrics":
                return showMetrics(cmdArgs);
            case "clear-scheduler":
                return clearScheduler(cmdArgs);
            default:
                System.err.println("Unknown scheduling command: " + command);
                printUsage();
                return 1;
        }
    }

    private int scheduleRoundRobin(String[] args) {
        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.err.println("Scheduler already running");
            return 1;
        }

        int timeQuantum = 100; // Default 100ms

        if (args.length > 0) {
            try {
                timeQuantum = Integer.parseInt(args[0]);
                if (timeQuantum <= 0) {
                    System.err.println("Time quantum must be positive");
                    return 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid time quantum: " + args[0]);
                return 1;
            }
        }

        currentScheduler = new RoundRobinScheduler(timeQuantum);
        System.out.println("Round-Robin scheduler initialized with time quantum: " + timeQuantum + "ms");
        System.out.println("Use 'add-process <name> <burst_time>' to add processes");
        System.out.println("Use 'run-scheduler' to start scheduling in background");
        return 0;
    }

    private int schedulePriority(String[] args) {
        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.err.println("Scheduler already running");
            return 1;
        }

        currentScheduler = new PriorityScheduler();
        System.out.println("Priority-Based scheduler initialized");
        System.out.println("Use 'add-process <name> <burst_time> <priority>' to add processes");
        System.out.println("Lower priority number = Higher priority");
        System.out.println("Use 'run-scheduler' to start scheduling in background");
        return 0;
    }

    private int addProcess(String[] args) {
        if (currentScheduler == null) {
            System.err.println("No scheduler initialized. Use 'schedule-rr' or 'schedule-priority' first.");
            return 1;
        }

        if (args.length < 2) {
            System.err.println("Usage: add-process <name> <burst_time> [priority]");
            return 1;
        }

        String name = args[0];
        int burstTime;
        int priority = 0; // Default priority

        try {
            burstTime = Integer.parseInt(args[1]);
            if (burstTime <= 0) {
                System.err.println("Burst time must be positive");
                return 1;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid burst time: " + args[1]);
            return 1;
        }

        if (args.length > 2) {
            try {
                priority = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid priority: " + args[2]);
                return 1;
            }
        }

        SimulatedProcess process = new SimulatedProcess(processIdCounter++, name, burstTime, priority);
        currentScheduler.addProcess(process);
        
        return 0;
    }

    private int runScheduler(String[] args) {
        if (currentScheduler == null) {
            System.err.println("No scheduler initialized. Use 'schedule-rr' or 'schedule-priority' first.");
            return 1;
        }

        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.err.println("Scheduler already running");
            return 1;
        }

        try {
            // Create logger
            SchedulerLogger logger = new SchedulerLogger();
            currentScheduler.setLogger(logger);

            // Create and start scheduler thread
            schedulerThread = new SchedulerThread(currentScheduler, logger);
            schedulerThread.start();

            System.out.println("Scheduler started in background. Logs: " + logger.getLogFileName());
            return 0;

        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
            return 1;
        }
    }

    private int stopScheduler(String[] args) {
        if (schedulerThread == null || !schedulerThread.isAlive()) {
            System.err.println("No scheduler is running");
            return 1;
        }

        System.out.println("Stopping scheduler...");
        schedulerThread.stopScheduler();

        // Wait for thread to finish
        try {
            schedulerThread.join(3000); // Wait up to 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Scheduler stopped. Incomplete processes remain in queue.");
        return 0;
    }

    private int showMetrics(String[] args) {
        if (currentScheduler == null) {
            System.err.println("No scheduler initialized.");
            return 1;
        }

        SchedulingMetrics metrics = currentScheduler.getMetrics();
        if (metrics.getCompletedProcesses().isEmpty()) {
            System.out.println("No completed processes yet.");
            return 0;
        }

        // Check if scheduler is still running
        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.out.println("Note: Scheduler still running - showing completed processes so far\n");
        }

        metrics.printMetrics();
        return 0;
    }

    private int clearScheduler(String[] args) {
        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.err.println("Cannot clear scheduler while running. Stop it first with stop-scheduler");
            return 1;
        }

        currentScheduler = null;
        schedulerThread = null;
        processIdCounter = 1;
        System.out.println("Scheduler cleared.");
        return 0;
    }

    private void printUsage() {
        System.out.println("\nScheduling Commands:");
        System.out.println("  schedule-rr [time_quantum]        - Initialize Round-Robin scheduler");
        System.out.println("  schedule-priority                 - Initialize Priority-Based scheduler");
        System.out.println("  add-process <name> <burst> [pri]  - Add a process (works before/during execution)");
        System.out.println("  run-scheduler                     - Start the scheduler in background");
        System.out.println("  stop-scheduler                    - Stop the running scheduler");
        System.out.println("  show-metrics                      - Display scheduling metrics");
        System.out.println("  clear-scheduler                   - Clear current scheduler (must be stopped)");
        System.out.println("\nExample - Round-Robin:");
        System.out.println("  schedule-rr 50");
        System.out.println("  add-process P1 200");
        System.out.println("  add-process P2 150");
        System.out.println("  run-scheduler");
        System.out.println("  show-metrics");
        System.out.println("\nExample - Priority with Dynamic Preemption:");
        System.out.println("  schedule-priority");
        System.out.println("  add-process Low 5000 10");
        System.out.println("  run-scheduler");
        System.out.println("  add-process High 1000 1    (will preempt Low immediately)");
        System.out.println("  show-metrics");
    }
}
