package com.osshell.core;

import com.osshell.commands.Command;
import com.osshell.commands.FileSystemCommands;
import com.osshell.commands.UtilityCommands;
import com.osshell.jobs.JobCommands;
import com.osshell.memory.MemoryCommands;
import com.osshell.process.ProcessManager;
import com.osshell.scheduling.SchedulingCommands;
import com.osshell.sync.SyncCommands;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes parsed commands to either built-in handlers or external process execution.
 */
public class CommandDispatcher {
    private final Map<String, Command> builtInCommands;
    private final ProcessManager processManager;
    private final FileSystemCommands fileSystemCommands;
    private final UtilityCommands utilityCommands;
    private final JobCommands jobCommands;
    private final SchedulingCommands schedulingCommands;
    private final MemoryCommands memoryCommands;
    private final SyncCommands syncCommands;

    public CommandDispatcher(ProcessManager processManager) {
        this.processManager = processManager;
        this.builtInCommands = new HashMap<>();
        this.fileSystemCommands = new FileSystemCommands();
        this.utilityCommands = new UtilityCommands();
        this.jobCommands = new JobCommands(processManager.getJobTracker());
        this.schedulingCommands = new SchedulingCommands();
        this.memoryCommands = new MemoryCommands();
        this.syncCommands = new SyncCommands();

        registerBuiltInCommands();
    }

    /**
     * Registers all built-in commands.
     */
    private void registerBuiltInCommands() {
        // File system commands
        builtInCommands.put("cd", fileSystemCommands);
        builtInCommands.put("pwd", fileSystemCommands);
        builtInCommands.put("ls", fileSystemCommands);
        builtInCommands.put("mkdir", fileSystemCommands);
        builtInCommands.put("rmdir", fileSystemCommands);
        builtInCommands.put("rm", fileSystemCommands);
        builtInCommands.put("touch", fileSystemCommands);
        builtInCommands.put("cat", fileSystemCommands);
        
        // Utility commands
        builtInCommands.put("echo", utilityCommands);
        builtInCommands.put("clear", utilityCommands);
        builtInCommands.put("exit", utilityCommands);
        
        // Job control commands
        builtInCommands.put("jobs", jobCommands);
        builtInCommands.put("fg", jobCommands);
        builtInCommands.put("bg", jobCommands);
        builtInCommands.put("kill", jobCommands);

        // Scheduling commands
        builtInCommands.put("schedule-rr", schedulingCommands);
        builtInCommands.put("schedule-priority", schedulingCommands);
        builtInCommands.put("add-process", schedulingCommands);
        builtInCommands.put("run-scheduler", schedulingCommands);
        builtInCommands.put("stop-scheduler", schedulingCommands);
        builtInCommands.put("show-metrics", schedulingCommands);
        builtInCommands.put("clear-scheduler", schedulingCommands);

        // Memory Management commands
        builtInCommands.put("mem-init", memoryCommands);
        builtInCommands.put("mem-alloc", memoryCommands);
        builtInCommands.put("mem-access", memoryCommands);
        builtInCommands.put("mem-free", memoryCommands);
        builtInCommands.put("mem-status", memoryCommands);
        builtInCommands.put("mem-algo", memoryCommands);

        // Synchronization commands
        builtInCommands.put("sync-pc-start", syncCommands);
        builtInCommands.put("sync-pc-stop", syncCommands);
    }

    /**
     * Dispatches the command to the appropriate handler.
     *
     * @param parsedCommand The parsed command to execute
     * @return Exit status code
     */
    public int dispatch(ParsedCommand parsedCommand) {
        if (parsedCommand == null) {
            return 0;
        }

        String commandName = parsedCommand.getCommandName();
        
        // Check if it's a built-in command
        if (builtInCommands.containsKey(commandName)) {
            Command command = builtInCommands.get(commandName);
            
            // Combine command name with arguments for FileSystemCommands, UtilityCommands, JobCommands, and SchedulingCommands
            if (command == fileSystemCommands || command == utilityCommands || command == jobCommands || command == schedulingCommands || command == memoryCommands || command == syncCommands) {
                String[] fullArgs = new String[parsedCommand.getArguments().length + 1];
                fullArgs[0] = commandName;
                System.arraycopy(parsedCommand.getArguments(), 0, fullArgs, 1, parsedCommand.getArguments().length);
                return command.execute(fullArgs);
            } else {
                return command.execute(parsedCommand.getArguments());
            }
        } else {
            // Execute as external process
            return processManager.executeProcess(parsedCommand);
        }
    }

    public UtilityCommands getUtilityCommands() {
        return utilityCommands;
    }
}
