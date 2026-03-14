package com.osshell.core;

/**
 * Data structure representing a parsed command.
 */
public class ParsedCommand {
    private final String commandName;
    private final String[] arguments;
    private final boolean runInBackground;

    public ParsedCommand(String commandName, String[] arguments, boolean runInBackground) {
        this.commandName = commandName;
        this.arguments = arguments;
        this.runInBackground = runInBackground;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public boolean isRunInBackground() {
        return runInBackground;
    }
}
