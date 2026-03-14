package com.osshell.sync;

import com.osshell.commands.Command;
import java.io.InputStream;
import java.io.PrintStream;

public class SyncCommands implements Command {
    private ProducerConsumerSimulation pcSimulation;

    @Override
    public int execute(String[] args) {
        return execute(args, System.in, System.out);
    }
    
    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        if (args.length == 0) {
            printUsage(out);
            return 1;
        }

        String subCommand = args[0];

        switch (subCommand) {
            case "sync-pc-start":
                if (args.length < 4) {
                    System.err.println("Usage: sync-pc-start <bufferSize> <producers> <consumers>");
                    return 1;
                }
                try {
                    int bufferSize = Integer.parseInt(args[1]);
                    int producers = Integer.parseInt(args[2]);
                    int consumers = Integer.parseInt(args[3]);
                    
                    if (pcSimulation != null) {
                        pcSimulation.stop();
                    }
                    
                    pcSimulation = new ProducerConsumerSimulation(bufferSize);
                    pcSimulation.start(producers, consumers);
                    // The simulation runs in background threads and prints to sysout directly
                    out.println("Simulation started.");
                    
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format.");
                    return 1;
                }
                break;

            case "sync-pc-stop":
                if (pcSimulation != null) {
                    pcSimulation.stop();
                    pcSimulation = null;
                } else {
                    out.println("No simulation running.");
                }
                break;

            default:
                System.err.println("Unknown sync command: " + subCommand);
                printUsage(out);
                return 1;
        }

        return 0;
    }

    private void printUsage(PrintStream out) {
        out.println("Synchronization Commands:");
        out.println("  sync-pc-start <bufferSize> <prod> <cons>   Start Producer-Consumer simulation");
        out.println("  sync-pc-stop                               Stop simulation");
    }
    
    private void printUsage() {
        printUsage(System.out);
    }
}
