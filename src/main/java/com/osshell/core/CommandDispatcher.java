package com.osshell.core;

import com.osshell.commands.Command;
import com.osshell.commands.FileSystemCommands;
import com.osshell.commands.UtilityCommands;
import com.osshell.commands.GrepCommand;
import com.osshell.commands.SortCommand;
import com.osshell.jobs.JobCommands;
import com.osshell.memory.MemoryCommands;
import com.osshell.process.ProcessManager;
import com.osshell.scheduling.SchedulingCommands;
import com.osshell.sync.SyncCommands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
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
    private final GrepCommand grepCommand;
    private final SortCommand sortCommand;

    public CommandDispatcher(ProcessManager processManager) {
        this.processManager = processManager;
        this.builtInCommands = new HashMap<>();
        this.fileSystemCommands = new FileSystemCommands();
        this.utilityCommands = new UtilityCommands();
        this.jobCommands = new JobCommands(processManager.getJobTracker());
        this.schedulingCommands = new SchedulingCommands();
        this.memoryCommands = new MemoryCommands();
        this.syncCommands = new SyncCommands();
        this.grepCommand = new GrepCommand();
        this.sortCommand = new SortCommand();

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
        builtInCommands.put("chmod", fileSystemCommands);
        builtInCommands.put("chown", fileSystemCommands);
        
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
        
        // Text processing commands
        builtInCommands.put("grep", grepCommand);
        builtInCommands.put("sort", sortCommand);
    }

    /**
     * Dispatches a single command with standard I/O.
     */
    public int dispatch(ParsedCommand parsedCommand) {
        return dispatch(parsedCommand, System.in, System.out);
    }

    /**
     * Executs a single command with specified I/O streams.
     */
    public int dispatch(ParsedCommand parsedCommand, InputStream in, PrintStream out) {
        if (parsedCommand == null) {
            return 0;
        }

        String commandName = parsedCommand.getCommandName();
        
        // Check if it's a built-in command
        if (builtInCommands.containsKey(commandName)) {
            Command command = builtInCommands.get(commandName);
            
            // Combine command name with arguments specific command types that rely on it
            if (command == fileSystemCommands || command == utilityCommands || command == jobCommands || command == schedulingCommands || command == memoryCommands || command == syncCommands) {
                String[] fullArgs = new String[parsedCommand.getArguments().length + 1];
                fullArgs[0] = commandName;
                System.arraycopy(parsedCommand.getArguments(), 0, fullArgs, 1, parsedCommand.getArguments().length);
                return command.execute(fullArgs, in, out);
            } else {
                return command.execute(parsedCommand.getArguments(), in, out);
            }
        } else {
            // Execute as external process - simplistic handling for now (doesn't support custom streams yet)
            // Ideally ProcessManager should accept streams too
            return processManager.executeProcess(parsedCommand);
        }
    }

    /**
     * Executes a pipeline of commands.
     */
    public int dispatchPipeline(List<ParsedCommand> pipeline) {
        if (pipeline.isEmpty()) return 0;
        
        InputStream currentIn = System.in;
        ByteArrayOutputStream buffer = null;
        
        for (int i = 0; i < pipeline.size(); i++) {
            ParsedCommand cmd = pipeline.get(i);
            boolean isLast = (i == pipeline.size() - 1);
            
            PrintStream currentOut;
            if (isLast) {
                currentOut = System.out;
            } else {
                buffer = new ByteArrayOutputStream();
                currentOut = new PrintStream(buffer);
            }
            
            int exitCode = dispatch(cmd, currentIn, currentOut);
            
            if (exitCode != 0) {
                return exitCode; // Stop chain on error
            }
            
            if (!isLast) {
                currentOut.flush();
                currentIn = new ByteArrayInputStream(buffer.toByteArray());
            }
        }
        
        return 0;
    }

    public UtilityCommands getUtilityCommands() {
        return utilityCommands;
    }
}
