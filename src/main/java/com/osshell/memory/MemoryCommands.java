package com.osshell.memory;

import com.osshell.commands.Command;

/**
 * Commands for interacting with the memory management system.
 */
public class MemoryCommands implements Command {

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        String subCommand = args[0];
        MemoryManager memoryManager = MemoryManager.getInstance();

        try {
            switch (subCommand) {
                case "mem-init":
                    if (args.length < 3) {
                        System.err.println("Usage: mem-init <totalSize> <pageSize>");
                        return 1;
                    }
                    int totalSize = Integer.parseInt(args[1]);
                    int pageSize = Integer.parseInt(args[2]);
                    memoryManager.init(totalSize, pageSize);
                    System.out.println("Memory initialized: " + totalSize + "KB total, " + pageSize + "KB pages.");
                    break;

                case "mem-alloc":
                    if (args.length < 3) {
                        System.err.println("Usage: mem-alloc <pid> <size>");
                        return 1;
                    }
                    int allocPid = Integer.parseInt(args[1]);
                    int allocSize = Integer.parseInt(args[2]);
                    memoryManager.allocateMemory(allocPid, allocSize);
                    break;

                case "mem-access":
                    if (args.length < 3) {
                        System.err.println("Usage: mem-access <pid> <address>");
                        return 1;
                    }
                    int accessPid = Integer.parseInt(args[1]);
                    int address = Integer.parseInt(args[2]);
                    memoryManager.accessMemory(accessPid, address);
                    break;

                case "mem-free":
                    if (args.length < 2) {
                        System.err.println("Usage: mem-free <pid>");
                        return 1;
                    }
                    int freePid = Integer.parseInt(args[1]);
                    memoryManager.deallocateMemory(freePid);
                    break;

                case "mem-status":
                    System.out.println(memoryManager.getMemoryStatus());
                    break;
                
                case "mem-algo":
                    if (args.length < 2) {
                        System.err.println("Usage: mem-algo <FIFO|LRU>");
                        return 1;
                    }
                    String algo = args[1].toUpperCase();
                    if (algo.equals("FIFO")) {
                        memoryManager.setAlgorithm(new FIFOAlgorithm());
                        System.out.println("Page replacement algorithm set to FIFO.");
                    } else if (algo.equals("LRU")) {
                        memoryManager.setAlgorithm(new LRUAlgorithm());
                        System.out.println("Page replacement algorithm set to LRU.");
                    } else {
                        System.err.println("Unknown algorithm: " + algo);
                    }
                    break;

                default:
                    System.err.println("Unknown memory command: " + subCommand);
                    printUsage();
                    return 1;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + e.getMessage());
            return 1;
        }

        return 0;
    }

    private void printUsage() {
        System.out.println("Memory Management Commands:");
        System.out.println("  mem-init <total> <page>  Initialize memory system");
        System.out.println("  mem-alloc <pid> <size>   Allocate virtual memory to process");
        System.out.println("  mem-access <pid> <addr>  Access virtual address (may trigger fault)");
        System.out.println("  mem-free <pid>           Deallocate process memory");
        System.out.println("  mem-status               Show memory map and stats");
        System.out.println("  mem-algo <FIFO|LRU>      Set page replacement algorithm");
    }
}


