package com.osshell.jobs;

/**
 * Standardizes error handling and feedback messages.
 */
public class ErrorHandler {

    /**
     * Handles command not found errors.
     */
    public static void commandNotFound(String command) {
        System.err.println(command + ": command not found");
    }

    /**
     * Handles invalid arguments errors.
     */
    public static void invalidArguments(String command, String message) {
        System.err.println(command + ": " + message);
    }

    /**
     * Handles process execution errors.
     */
    public static void processExecutionError(String command, String message) {
        System.err.println(command + ": execution error: " + message);
    }

    /**
     * Handles file system errors.
     */
    public static void fileSystemError(String operation, String path, String message) {
        System.err.println(operation + ": " + path + ": " + message);
    }

    /**
     * Handles job control errors.
     */
    public static void jobControlError(String operation, String jobId, String message) {
        System.err.println(operation + ": " + jobId + ": " + message);
    }

    /**
     * Handles generic errors.
     */
    public static void genericError(String message) {
        System.err.println("Error: " + message);
    }

    /**
     * Prints a warning message.
     */
    public static void warning(String message) {
        System.err.println("Warning: " + message);
    }
}
