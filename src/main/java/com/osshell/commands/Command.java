package com.osshell.commands;

/**
 * Interface defining the contract for all shell commands.
 * All built-in commands must implement this interface.
 */
public interface Command {
    /**
     * Executes the command with the provided arguments.
     *
     * @param args Command arguments (excluding the command name itself)
     * @return Exit status code (0 for success, non-zero for error)
     */
    int execute(String[] args);
}
