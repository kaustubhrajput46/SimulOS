package com.osshell.commands;

import com.osshell.core.Shell;

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

    private int echo(String[] args) {
        if (args.length == 0) {
            System.out.println();
            return 0;
        }

        System.out.println(String.join(" ", args));
        return 0;
    }

    private int clear(String[] args) {
        try {
            // ANSI escape code to clear screen and move cursor to top-left
            System.out.print("\033[H\033[2J");
            System.out.flush();
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
