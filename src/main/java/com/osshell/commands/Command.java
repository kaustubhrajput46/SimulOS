package com.osshell.commands;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Interface defining the contract for all shell commands.
 * All built-in commands must implement this interface.
 */
public interface Command {
    /**
     * Executes the command with the provided arguments and I/O streams.
     *
     * @param args Command arguments (excluding the command name itself)
     * @param in Input stream (stdin)
     * @param out Output stream (stdout)
     * @return Exit status code (0 for success, non-zero for error)
     */
    int execute(String[] args, InputStream in, PrintStream out);

    /**
     * Legacy support method, defaults to System.in and System.out.
     */
    default int execute(String[] args) {
        return execute(args, System.in, System.out);
    }
}
