package com.osshell.sync;

import com.osshell.commands.Command;

public class SyncCommands implements Command {
    private ProducerConsumerSimulation pcSimulation;

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            printUsage();
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
                    System.out.println("No simulation running.");
                }
                break;

            default:
                System.err.println("Unknown sync command: " + subCommand);
                printUsage();
                return 1;
        }

        return 0;
    }

    private void printUsage() {
        System.out.println("Synchronization Commands:");
        System.out.println("  sync-pc-start <bufferSize> <prod> <cons>   Start Producer-Consumer simulation");
        System.out.println("  sync-pc-stop                               Stop simulation");
    }
}



