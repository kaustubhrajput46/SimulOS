package com.osshell.jobs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe job tracker that maps internal Job IDs to Process information.
 */
public class JobTracker {
    private final Map<Integer, JobInfo> jobs;
    private final AtomicInteger nextJobId;

    public JobTracker() {
        this.jobs = new ConcurrentHashMap<>();
        this.nextJobId = new AtomicInteger(1);
    }

    /**
     * Adds a new job to the tracker.
     *
     * @param process The Java Process object
     * @param command The command string
     * @return The assigned job ID
     */
    public int addJob(Process process, String command) {
        int jobId = nextJobId.getAndIncrement();
        JobInfo jobInfo = new JobInfo(jobId, process, command);
        jobs.put(jobId, jobInfo);
        return jobId;
    }

    /**
     * Removes a job from the tracker.
     *
     * @param jobId The job ID to remove
     */
    public void removeJob(int jobId) {
        jobs.remove(jobId);
    }

    /**
     * Gets job information by job ID.
     *
     * @param jobId The job ID
     * @return JobInfo or null if not found
     */
    public JobInfo getJob(int jobId) {
        return jobs.get(jobId);
    }

    /**
     * Gets all jobs.
     *
     * @return Map of all jobs
     */
    public Map<Integer, JobInfo> getAllJobs() {
        return new ConcurrentHashMap<>(jobs);
    }

    /**
     * Cleans up completed jobs from the tracker.
     */
    public void cleanupCompletedJobs() {
        jobs.entrySet().removeIf(entry -> !entry.getValue().getProcess().isAlive());
    }

    /**
     * Information about a background job.
     */
    public static class JobInfo {
        private final int jobId;
        private final Process process;
        private final String command;
        private final long startTime;
        private JobState state;

        public JobInfo(int jobId, Process process, String command) {
            this.jobId = jobId;
            this.process = process;
            this.command = command;
            this.startTime = System.currentTimeMillis();
            this.state = JobState.RUNNING;
        }

        public int getJobId() {
            return jobId;
        }

        public Process getProcess() {
            return process;
        }

        public String getCommand() {
            return command;
        }

        public long getStartTime() {
            return startTime;
        }

        public JobState getState() {
            return state;
        }

        public void setState(JobState state) {
            this.state = state;
        }

        public long getPid() {
            return process.pid();
        }
    }

    public enum JobState {
        RUNNING,
        STOPPED,
        DONE
    }
}
