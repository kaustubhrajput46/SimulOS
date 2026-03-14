package com.osshell.process;

import com.osshell.core.ParsedCommand;
import com.osshell.jobs.JobTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the creation and execution of external processes.
 */
public class ProcessManager {
    private final JobTracker jobTracker;

    public ProcessManager() {
        this.jobTracker = new JobTracker();
    }

    /**
     * Executes an external process based on the parsed command.
     *
     * @param parsedCommand The parsed command to execute
     * @return Exit status code
     */
    public int executeProcess(ParsedCommand parsedCommand) {
        String commandName = parsedCommand.getCommandName();
        String[] arguments = parsedCommand.getArguments();
        boolean runInBackground = parsedCommand.isRunInBackground();

        // Build command list
        List<String> commandList = new ArrayList<>();
        commandList.add(commandName);
        for (String arg : arguments) {
            commandList.add(arg);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.directory(new java.io.File(System.getProperty("user.dir")));

            if (runInBackground) {
                // Run in background
                processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
                
                Process process = processBuilder.start();
                int jobId = jobTracker.addJob(process, String.join(" ", commandList));
                
                System.out.println("[" + jobId + "] " + process.pid());
                
                // Start a thread to monitor the background process
                new Thread(() -> monitorBackgroundProcess(jobId, process)).start();
                
                return 0;
            } else {
                // Run in foreground
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                
                int exitCode = process.waitFor();
                return exitCode;
            }

        } catch (IOException e) {
            System.err.println(commandName + ": command not found");
            return 127;
        } catch (InterruptedException e) {
            System.err.println(commandName + ": process interrupted");
            Thread.currentThread().interrupt();
            return 130;
        }
    }

    /**
     * Monitors a background process and reports when it completes.
     */
    private void monitorBackgroundProcess(int jobId, Process process) {
        try {
            int exitCode = process.waitFor();
            
            // Consume any remaining output
            consumeProcessOutput(process);
            
            jobTracker.removeJob(jobId);
            System.out.println("\n[" + jobId + "] Done");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Consumes and discards process output.
     */
    private void consumeProcessOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Output consumed but not displayed for background processes
            }
            while ((line = errorReader.readLine()) != null) {
                // Error output consumed
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Brings a background job to the foreground.
     *
     * @param jobId The job ID
     * @return Exit code of the process
     */
    public int bringToForeground(int jobId) {
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

    public JobTracker getJobTracker() {
        return jobTracker;
    }
}
