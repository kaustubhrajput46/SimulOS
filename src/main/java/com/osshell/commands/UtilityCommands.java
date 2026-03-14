package com.osshell.commands;

import com.osshell.core.Shell;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Implements utility commands: echo, clear, exit.
 */
public class UtilityCommands implements Command {
    private Shell shell;

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "echo":
                return echo(cmdArgs);
            case "clear":
                return clear(cmdArgs);
            case "exit":
                return exit(cmdArgs);
            default:
                System.err.println("Unknown utility command: " + command);
                return 1;
        }
    }

    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        if (args.length == 0) {
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "echo":
                return echo(cmdArgs, out);
            case "clear":
                return clear(cmdArgs, out);
            case "exit":
                return exit(cmdArgs); // Exit doesn't need streams usually
            default:
                System.err.println("Unknown utility command: " + command);
                return 1;
        }
    }

    private int echo(String[] args, PrintStream out) {
        if (args.length == 0) {
            out.println();
            return 0;
        }

        out.println(String.join(" ", args));
        return 0;
    }

    private int clear(String[] args, PrintStream out) {
        try {
            // ANSI escape code to clear screen and move cursor to top-left
            out.print("\033[H\033[2J");
            out.flush();
            return 0;
        } catch (Exception e) {
            System.err.println("clear: error clearing screen");
            return 1;
        }
    }

    private int exit(String[] args) {
        int exitCode = 0;
        
        if (args.length > 0) {
            try {
                exitCode = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("exit: " + args[0] + ": numeric argument required");
                exitCode = 2;
            }
        }

        if (shell != null) {
            shell.stop();
        }
        
        return exitCode;
    }
}
