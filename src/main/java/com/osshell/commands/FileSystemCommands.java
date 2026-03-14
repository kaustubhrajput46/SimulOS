package com.osshell.commands;

import com.osshell.security.FileSecurityManager;
import com.osshell.security.Session;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
    private final FileSecurityManager securityManager;

    public FileSystemCommands() {
        this.currentDirectory = Paths.get(System.getProperty("user.dir"));
        this.securityManager = FileSecurityManager.getInstance();
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
            case "chmod":
                return chmod(cmdArgs);
            case "chown":
                return chown(cmdArgs);
            default:
                System.err.println("Unknown file system command: " + command);
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
            case "cd":
                return cd(cmdArgs, out);
            case "pwd":
                return pwd(cmdArgs, out);
            case "ls":
                return ls(cmdArgs, out);
            case "mkdir":
                return mkdir(cmdArgs, out);
            case "rmdir":
                return rmdir(cmdArgs, out);
            case "rm":
                return rm(cmdArgs, out);
            case "touch":
                return touch(cmdArgs, out);
            case "cat":
                return cat(cmdArgs, out);
            case "chmod":
                return chmod(cmdArgs, out);
            case "chown":
                return chown(cmdArgs, out);
            default:
                System.err.println("Unknown file system command: " + command);
                return 1;
        }
    }

    private int cd(String[] args, PrintStream out) {
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
        
        if (!checkAccess(newPath, "x")) {
            System.err.println("cd: permission denied: " + targetPath);
            return 1;
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

    private int pwd(String[] args, PrintStream out) {
        out.println(currentDirectory.toAbsolutePath());
        return 0;
    }

    private int ls(String[] args, PrintStream out) {
        Path targetPath = args.length > 0 ? resolvePath(args[0]) : currentDirectory;
        
        if (!checkAccess(targetPath, "r")) {
            System.err.println("ls: permission denied: " + targetPath);
            return 1;
        }

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
                                out.println(name + "/");
                            } else {
                                out.println(name);
                            }
                        });
                return 0;
            } catch (IOException e) {
                System.err.println("ls: error reading directory: " + e.getMessage());
                return 1;
            }
        } else {
            out.println(targetPath.getFileName());
            return 0;
        }
    }

    private int mkdir(String[] args, PrintStream out) {
        if (args.length == 0) {
            System.err.println("mkdir: missing operand");
            return 1;
        }

        for (String dirName : args) {
            Path newDir = resolvePath(dirName);
            
            if (!checkAccess(newDir.getParent(), "w")) {
                System.err.println("mkdir: permission denied: " + dirName);
                return 1;
            }

            try {
                Files.createDirectories(newDir);
                securityManager.createFileMetadata(newDir.toString(), Session.getInstance().getCurrentUser().getUsername());
            } catch (IOException e) {
                System.err.println("mkdir: cannot create directory '" + dirName + "': " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    private int rmdir(String[] args, PrintStream out) {
        if (args.length == 0) {
            System.err.println("rmdir: missing operand");
            return 1;
        }

        for (String dirName : args) {
            Path dir = resolvePath(dirName);
            
            if (!checkAccess(dir.getParent(), "w")) {
                System.err.println("rmdir: permission denied: " + dirName);
                return 1;
            }

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

    private int rm(String[] args, PrintStream out) {
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
            
            if (!checkAccess(file.getParent(), "w")) {
                System.err.println("rm: permission denied: " + args[i]);
                return 1;
            }

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

    private int touch(String[] args, PrintStream out) {
        if (args.length == 0) {
            System.err.println("touch: missing operand");
            return 1;
        }

        for (String fileName : args) {
            Path file = resolvePath(fileName);
            
            // Check write permission on parent directory (to create or access)
            if (!checkAccess(file.getParent(), "w")) {
                 System.err.println("touch: permission denied: " + fileName);
                 return 1;
            }

            try {
                if (!Files.exists(file)) {
                    Files.createFile(file);
                    securityManager.createFileMetadata(file.toString(), Session.getInstance().getCurrentUser().getUsername());
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

    private int cat(String[] args, PrintStream out) {
        if (args.length == 0) {
            System.err.println("cat: missing operand");
            return 1;
        }

        for (String fileName : args) {
            Path file = resolvePath(fileName);
            
            if (!checkAccess(file, "r")) {
                System.err.println("cat: permission denied: " + fileName);
                return 1;
            }

            if (!Files.exists(file)) {
                System.err.println("cat: " + fileName + ": No such file or directory");
                return 1;
            }
            if (Files.isDirectory(file)) {
                System.err.println("cat: " + fileName + ": Is a directory");
                return 1;
            }

            try (Stream<String> lines = Files.lines(file)) {
                lines.forEach(out::println);
            } catch (IOException e) {
                System.err.println("cat: " + fileName + ": " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

    private int chmod(String[] args, PrintStream out) {
        if (args.length < 2) {
            System.err.println("Usage: chmod <permissions> <file>");
            return 1;
        }
        
        String perms = args[0];
        Path file = resolvePath(args[1]);
        
        if (!Files.exists(file)) {
            System.err.println("chmod: " + args[1] + ": No such file");
            return 1;
        }
        
        // Only owner or admin can change permissions
        // We cheat and use 'w' to represent ownership check roughly for now, or implement strict owner check
        if (!Session.getInstance().getCurrentUser().getUsername().equals(securityManager.getFileDetails(file.toString()).split(" ")[1]) 
            && Session.getInstance().getCurrentUser().getRole() != com.osshell.security.Role.ADMIN) {
             System.err.println("chmod: changing permissions of '" + args[1] + "': Operation not permitted");
             return 1;
        }
        
        securityManager.setPermissions(file.toString(), securityManager.getFileDetails(file.toString()).split(" ")[1], perms);
        return 0;
    }

    private int chown(String[] args, PrintStream out) {
        if (args.length < 2) {
             System.err.println("Usage: chown <owner> <file>");
             return 1;
        }
        
        // Only admin can chown
        if (Session.getInstance().getCurrentUser().getRole() != com.osshell.security.Role.ADMIN) {
            System.err.println("chown: operation not permitted");
            return 1;
        }
        
        String owner = args[0];
        Path file = resolvePath(args[1]);
        String currentPerms = securityManager.getFileDetails(file.toString()).split(" ")[0];
        securityManager.setPermissions(file.toString(), owner, currentPerms);
        return 0;
    }

    private boolean checkAccess(Path path, String mode) {
        if (!Session.getInstance().isLoggedIn()) return false;
        
        // If checking parent and it's null (root), assume allowed for now or handle appropriately
        if (path == null) return true;
        
        switch (mode) {
            case "r": return securityManager.canRead(path.toString(), Session.getInstance().getCurrentUser());
            case "w": return securityManager.canWrite(path.toString(), Session.getInstance().getCurrentUser());
            case "x": return securityManager.canExecute(path.toString(), Session.getInstance().getCurrentUser());
            default: return false;
        }
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
