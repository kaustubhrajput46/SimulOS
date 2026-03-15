package com.osshell.jobs;

import com.osshell.commands.Command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

/**
 * Implements job control commands: jobs, fg, bg, kill.
 */
public class JobCommands implements Command {
    private final JobTracker jobTracker;

    public JobCommands(JobTracker jobTracker) {
        this.jobTracker = jobTracker;
    }

    @Override
    public int execute(String[] args) {
        return execute(args, System.in, System.out);
    }

    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        if (args.length == 0) {
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "jobs":
                return listJobs(out);
            case "fg":
                return fg(cmdArgs);
            case "bg":
                return bg(cmdArgs);
            case "kill":
                return kill(cmdArgs);
            default:
                System.err.println("Unknown job command: " + command);
                return 1;
        }
    }

    private int listJobs(PrintStream out) {
        // Clean up completed jobs first
        jobTracker.cleanupCompletedJobs();

        Map<Integer, JobTracker.JobInfo> allJobs = jobTracker.getAllJobs();
        
        if (allJobs.isEmpty()) {
            out.println("No active jobs.");
            return 0;
        }

        out.println("ID\tPID\tState\tCommand");
        for (JobTracker.JobInfo jobInfo : allJobs.values()) {
            String state = jobInfo.getProcess().isAlive() ? "Running" : "Done";
            out.printf("[%d]\t%d\t%s\t%s%n",
                    jobInfo.getJobId(),
                    jobInfo.getPid(),
                    state,
                    jobInfo.getCommand());
        }
        return 0;
    }

    private int fg(String[] args) {
        if (args.length == 0) {
            System.err.println("fg: current: no such job");
            return 1;
        }

        int jobId;
        try {
            jobId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("fg: " + args[0] + ": no such job");
            return 1;
        }

        JobTracker.JobInfo jobInfo = jobTracker.getJob(jobId);
        if (jobInfo == null) {
            System.err.println("fg: " + jobId + ": no such job");
            return 1;
        }

        Process process = jobInfo.getProcess();
        if (!process.isAlive()) {
            System.err.println("fg: " + jobId + ": job has completed");
            jobTracker.removeJob(jobId);
            return 1;
        }

        System.out.println(jobInfo.getCommand());

        try {
            int exitCode = process.waitFor();
            jobTracker.removeJob(jobId);
            return exitCode;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 130;
        }
    }

    private int bg(String[] args) {
        if (args.length == 0) {
            System.err.println("bg: current: no such job");
            return 1;
        }

        int jobId;
        try {
            jobId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("bg: " + args[0] + ": no such job");
            return 1;
        }

        JobTracker.JobInfo jobInfo = jobTracker.getJob(jobId);
        if (jobInfo == null) {
            System.err.println("bg: " + jobId + ": no such job");
            return 1;
        }

        if (!jobInfo.getProcess().isAlive()) {
            System.err.println("bg: " + jobId + ": job has completed");
            jobTracker.removeJob(jobId);
            return 1;
        }

        jobInfo.setState(JobTracker.JobState.RUNNING);
        System.out.println("[" + jobId + "] " + jobInfo.getCommand() + " &");

        return 0;
    }

    private int kill(String[] args) {
        if (args.length == 0) {
            System.err.println("kill: usage: kill pid");
            return 1;
        }

        long pid;
        try {
            pid = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("kill: " + args[0] + ": arguments must be process or job IDs");
            return 1;
        }

        // Try to find and kill by PID in our job tracker
        JobTracker.JobInfo targetJob = null;
        for (JobTracker.JobInfo jobInfo : jobTracker.getAllJobs().values()) {
            if (jobInfo.getPid() == pid) {
                targetJob = jobInfo;
                break;
            }
        }

        if (targetJob != null) {
            targetJob.getProcess().destroy();
            jobTracker.removeJob(targetJob.getJobId());
            return 0;
        }

        // If not found in our jobs, try using ProcessHandle
        try {
            ProcessHandle.of(pid).ifPresentOrElse(
                handle -> handle.destroy(),
                () -> System.err.println("kill: (" + pid + ") - No such process")
            );
            return 0;
        } catch (Exception e) {
            System.err.println("kill: (" + pid + ") - " + e.getMessage());
            return 1;
        }
    }
}
