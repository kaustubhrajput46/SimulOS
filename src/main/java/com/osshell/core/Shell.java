package com.osshell.core;

import com.osshell.process.ProcessManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main entry point for the custom OS shell.
 * Implements the Read-Eval-Print Loop (REPL).
 */
public class Shell {
    private static final String PROMPT = "simulos> ";
    private final CommandParser parser;
    private final CommandDispatcher dispatcher;
    private boolean running;

    public Shell() {
        this.parser = new CommandParser();
        ProcessManager processManager = new ProcessManager();
        this.dispatcher = new CommandDispatcher(processManager);
        this.running = true;
    }

    /**
     * Main REPL loop.
     */
    public void run() {
        System.out.println("SimulOS Shell - Version 1.0");
        System.out.println("Type 'exit' to quit.");
        System.out.println();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (running) {
            try {
                System.out.print(PROMPT);
                String input = reader.readLine();

                if (input == null) {
                    // EOF reached (Ctrl+D)
                    break;
                }

                // Parse the input
                ParsedCommand parsedCommand = parser.parse(input);

                // Execute the command
                if (parsedCommand != null) {
                    int exitCode = dispatcher.dispatch(parsedCommand);
                    
                    // Check if exit command was executed
                    if (!running) {
                        break;
                    }
                }

            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Goodbye!");
    }

    /**
     * Stops the shell.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        Shell shell = new Shell();
        // Make the shell instance accessible to UtilityCommands
        shell.dispatcher.getUtilityCommands().setShell(shell);
        shell.run();
    }
}
