package com.osshell.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Implements file system operations: cd, pwd, ls, mkdir, rmdir, rm, touch, cat.
 */
public class FileSystemCommands implements Command {
    private Path currentDirectory;

    public FileSystemCommands() {
        this.currentDirectory = Paths.get(System.getProperty("user.dir"));
    }

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            return 1;
        }

        String command = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "cd":
                return cd(cmdArgs);
            case "pwd":
                return pwd(cmdArgs);
            case "ls":
                return ls(cmdArgs);
            case "mkdir":
                return mkdir(cmdArgs);
            case "rmdir":
                return rmdir(cmdArgs);
            case "rm":
                return rm(cmdArgs);
            case "touch":
                return touch(cmdArgs);
            case "cat":
                return cat(cmdArgs);
            default:
                System.err.println("Unknown file system command: " + command);
                return 1;
        }
    }

    private int cd(String[] args) {
        if (args.length == 0) {
            // Change to home directory
            currentDirectory = Paths.get(System.getProperty("user.home"));
            System.setProperty("user.dir", currentDirectory.toString());
            return 0;
        }

        String targetPath = args[0];
        Path newPath;

        if (targetPath.equals("~")) {
            newPath = Paths.get(System.getProperty("user.home"));
        } else if (Paths.get(targetPath).isAbsolute()) {
            newPath = Paths.get(targetPath);
        } else {
            newPath = currentDirectory.resolve(targetPath).normalize();
        }

        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            currentDirectory = newPath;
            System.setProperty("user.dir", currentDirectory.toString());
            return 0;
        } else {
            System.err.println("cd: " + targetPath + ": No such directory");
            return 1;
        }
    }

    private int pwd(String[] args) {
        System.out.println(currentDirectory.toAbsolutePath());
        return 0;
    }

    private int ls(String[] args) {
        Path targetPath = args.length > 0 ? resolvePath(args[0]) : currentDirectory;

        if (!Files.exists(targetPath)) {
            System.err.println("ls: cannot access '" + args[0] + "': No such file or directory");
            return 1;
        }

        if (Files.isDirectory(targetPath)) {
            try {
                Files.list(targetPath)
                        .sorted(Comparator.comparing(Path::getFileName))
                        .forEach(path -> {
                            String name = path.getFileName().toString();
                            if (Files.isDirectory(path)) {
                                System.out.println(name + "/");
                            } else {
                                System.out.println(name);
                            }
                        });
                return 0;
            } catch (IOException e) {
                System.err.println("ls: error reading directory: " + e.getMessage());
                return 1;
            }
        } else {
            System.out.println(targetPath.getFileName());
            return 0;
        }
    }

    private int mkdir(String[] args) {
        if (args.length == 0) {
            System.err.println("mkdir: missing operand");
            return 1;
        }

        for (String dirName : args) {
            Path newDir = resolvePath(dirName);
            try {
                Files.createDirectories(newDir);
            } catch (IOException e) {
                System.err.println("mkdir: cannot create directory '" + dirName + "': " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    private int rmdir(String[] args) {
        if (args.length == 0) {
            System.err.println("rmdir: missing operand");
            return 1;
        }

        for (String dirName : args) {
            Path dir = resolvePath(dirName);
            if (!Files.exists(dir)) {
                System.err.println("rmdir: failed to remove '" + dirName + "': No such file or directory");
                return 1;
            }
            if (!Files.isDirectory(dir)) {
                System.err.println("rmdir: failed to remove '" + dirName + "': Not a directory");
                return 1;
            }
            try {
                Files.delete(dir);
            } catch (IOException e) {
                System.err.println("rmdir: failed to remove '" + dirName + "': Directory not empty");
                return 1;
            }
        }
        return 0;
    }

    private int rm(String[] args) {
        if (args.length == 0) {
            System.err.println("rm: missing operand");
            return 1;
        }

        boolean recursive = false;
        int startIndex = 0;

        if (args[0].equals("-r") || args[0].equals("-rf")) {
            recursive = true;
            startIndex = 1;
        }

        if (startIndex >= args.length) {
            System.err.println("rm: missing operand");
            return 1;
        }

        for (int i = startIndex; i < args.length; i++) {
            Path file = resolvePath(args[i]);
            if (!Files.exists(file)) {
                System.err.println("rm: cannot remove '" + args[i] + "': No such file or directory");
                return 1;
            }

            try {
                if (Files.isDirectory(file) && recursive) {
                    // Delete directory recursively
                    try (Stream<Path> walk = Files.walk(file)) {
                        walk.sorted(Comparator.reverseOrder())
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        System.err.println("rm: cannot remove '" + path + "': " + e.getMessage());
                                    }
                                });
                    }
                } else if (Files.isDirectory(file)) {
                    System.err.println("rm: cannot remove '" + args[i] + "': Is a directory");
                    return 1;
                } else {
                    Files.delete(file);
                }
            } catch (IOException e) {
                System.err.println("rm: cannot remove '" + args[i] + "': " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    private int touch(String[] args) {
        if (args.length == 0) {
            System.err.println("touch: missing operand");
            return 1;
        }

        for (String fileName : args) {
            Path file = resolvePath(fileName);
            try {
                if (!Files.exists(file)) {
                    Files.createFile(file);
                } else {
                    // Update last modified time
                    Files.setLastModifiedTime(file, 
                        java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));
                }
            } catch (IOException e) {
                System.err.println("touch: cannot touch '" + fileName + "': " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    private int cat(String[] args) {
        if (args.length == 0) {
            System.err.println("cat: missing operand");
            return 1;
        }

        for (String fileName : args) {
            Path file = resolvePath(fileName);
            if (!Files.exists(file)) {
                System.err.println("cat: " + fileName + ": No such file or directory");
                return 1;
            }
            if (Files.isDirectory(file)) {
                System.err.println("cat: " + fileName + ": Is a directory");
                return 1;
            }

            try {
                Files.lines(file).forEach(System.out::println);
            } catch (IOException e) {
                System.err.println("cat: " + fileName + ": " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    /**
     * Resolves a path relative to the current directory.
     */
    private Path resolvePath(String path) {
        Path p = Paths.get(path);
        if (p.isAbsolute()) {
            return p;
        }
        return currentDirectory.resolve(path).normalize();
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }
}
