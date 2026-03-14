package com.osshell.core;

import com.osshell.process.ProcessManager;
import com.osshell.security.Session;
import com.osshell.security.User;
import com.osshell.security.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the custom OS shell.
 * Implements the Read-Eval-Print Loop (REPL).
 */
public class Shell {
    private static final String PROMPT = "simulos> ";
    private final CommandParser parser;
    private final CommandDispatcher dispatcher;
    private final UserManager userManager;
    private boolean running;

    public Shell() {
        this.parser = new CommandParser();
        ProcessManager processManager = new ProcessManager();
        this.dispatcher = new CommandDispatcher(processManager);
        this.userManager = new UserManager();
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

        // Login Loop
        if (!login(reader)) {
            System.out.println("Authentication failed. Exiting.");
            return;
        }

        while (running) {
            try {
                User user = Session.getInstance().getCurrentUser();
                System.out.print("[" + user.getUsername() + "] " + PROMPT);
                String input = reader.readLine();

                if (input == null) {
                    // EOF reached (Ctrl+D)
                    break;
                }

                // pipeline support
                if (input.contains("|")) {
                    String[] segments = input.split("\\|");
                    List<ParsedCommand> pipeline = new ArrayList<>();
                    boolean parseError = false;
                    
                    for (String segment : segments) {
                        ParsedCommand cmd = parser.parse(segment);
                        if (cmd != null) {
                            pipeline.add(cmd);
                        } else {
                            // Empty segment or error
                            parseError = true;
                            break;
                        }
                    }
                    
                    if (!parseError && !pipeline.isEmpty()) {
                        dispatcher.dispatchPipeline(pipeline);
                    }
                } else {
                    // Normal single command
                    ParsedCommand parsedCommand = parser.parse(input);

                    // Execute the command
                    if (parsedCommand != null) {
                        int exitCode = dispatcher.dispatch(parsedCommand);
                        
                        // Check if exit command was executed
                        if (!running) {
                            break;
                        }
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

    private boolean login(BufferedReader reader) {
        System.out.println("--- Login Required ---");
        int attempts = 0;
        
        while (attempts < 3) {
            try {
                System.out.print("Username: ");
                String username = reader.readLine();
                
                System.out.print("Password: ");
                // Ideally use System.console().readPassword() if available, but for simple stdin stream we use readLine
                String password = reader.readLine();
                
                User user = userManager.authenticate(username, password);
                if (user != null) {
                    Session.getInstance().login(user);
                    System.out.println("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                    return true;
                } else {
                    System.out.println("Invalid credentials.");
                    attempts++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
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
