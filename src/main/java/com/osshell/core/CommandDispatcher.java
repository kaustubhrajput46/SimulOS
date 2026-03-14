package com.osshell.core;

import com.osshell.commands.Command;
import com.osshell.commands.FileSystemCommands;
import com.osshell.commands.UtilityCommands;
import com.osshell.jobs.ErrorHandler;
import com.osshell.jobs.JobCommands;
import com.osshell.process.ProcessManager;

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

    public CommandDispatcher(ProcessManager processManager) {
        this.processManager = processManager;
        this.builtInCommands = new HashMap<>();
        this.fileSystemCommands = new FileSystemCommands();
        this.utilityCommands = new UtilityCommands();
        this.jobCommands = new JobCommands(processManager.getJobTracker());
        
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
            
            // Combine command name with arguments for FileSystemCommands and UtilityCommands
            if (command == fileSystemCommands || command == utilityCommands || command == jobCommands) {
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
