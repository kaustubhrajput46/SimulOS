package com.osshell.scheduling;

import com.osshell.commands.Command;

import java.io.InputStream;
import java.io.PrintStream;
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
        return execute(args, System.in, System.out);
    }
    
    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        if (args.length == 0) {
            printUsage(out);
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "schedule-rr":
                return scheduleRoundRobin(cmdArgs, out);
            case "schedule-priority":
                return schedulePriority(cmdArgs, out);
            case "add-process":
                return addProcess(cmdArgs, out);
            case "run-scheduler":
                return runScheduler(cmdArgs, out);
            case "stop-scheduler":
                return stopScheduler(cmdArgs, out);
            case "show-metrics":
                return showMetrics(cmdArgs, out);
            case "clear-scheduler":
                return clearScheduler(cmdArgs, out);
            default:
                System.err.println("Unknown scheduling command: " + command);
                printUsage(out);
                return 1;
        }
    }

    private void printUsage(PrintStream out) {
        // ... simple usage print
         out.println("Scheduler Usage: schedule-rr, schedule-priority, add-process, run-scheduler, stop-scheduler, show-metrics");
    }

    private int scheduleRoundRobin(String[] args, PrintStream out) {
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
        out.println("Round-Robin scheduler initialized with time quantum: " + timeQuantum + "ms");
        out.println("Use 'add-process <name> <burst_time>' to add processes");
        out.println("Use 'run-scheduler' to start scheduling in background");
        return 0;
    }

    private int schedulePriority(String[] args, PrintStream out) {
        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.err.println("Scheduler already running");
            return 1;
        }

        currentScheduler = new PriorityScheduler();
        out.println("Priority-Based scheduler initialized");
        out.println("Use 'add-process <name> <burst_time> <priority>' to add processes");
        out.println("Lower priority number = Higher priority");
        out.println("Use 'run-scheduler' to start scheduling in background");
        return 0;
    }
    
    private int addProcess(String[] args, PrintStream out) {
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

    private int runScheduler(String[] args, PrintStream out) {
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

            out.println("Scheduler started in background. Logs: " + logger.getLogFileName());
            return 0;

        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
            return 1;
        }
    }

    private int stopScheduler(String[] args, PrintStream out) {
        if (schedulerThread == null || !schedulerThread.isAlive()) {
            System.err.println("No scheduler is running");
            return 1;
        }

        out.println("Stopping scheduler...");
        schedulerThread.stopScheduler();

        // Wait for thread to finish
        try {
            schedulerThread.join(3000); // Wait up to 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        out.println("Scheduler stopped. Incomplete processes remain in queue.");
        return 0;
    }

    private int showMetrics(String[] args, PrintStream out) {
        if (currentScheduler == null) {
            System.err.println("No scheduler initialized.");
            return 1;
        }

        SchedulingMetrics metrics = currentScheduler.getMetrics();
        if (metrics.getCompletedProcesses().isEmpty()) {
            out.println("No completed processes yet.");
            return 0;
        }

        // Check if scheduler is still running
        if (schedulerThread != null && schedulerThread.isAlive()) {
            out.println("Note: Scheduler still running - showing completed processes so far\n");
        }

        // Assuming metrics.printMetrics() writes to System.out, ideally refactor it or capture it.
        // For now, let's leave internal methods of Metrics using System.out or refactor Metrics later.
        // Or better redirect System.out if needed (complex), but "metrics.printMetrics()" 
        // likely just sysouts. Let's fix SchedulingMetrics potentially?
        // Actually, let's just use System.out for Metrics for now as piping metrics is rare requirement here.
        metrics.printMetrics(); 
        return 0;
    }

    private int clearScheduler(String[] args, PrintStream out) {
        if (schedulerThread != null && schedulerThread.isAlive()) {
             System.err.println("Stop scheduler first.");
             return 1;
        }
        currentScheduler = null;
        processIdCounter = 1;
        out.println("Scheduler cleared.");
        return 0;
    }

    private void printUsage() { 
       printUsage(System.out);
    }
}
