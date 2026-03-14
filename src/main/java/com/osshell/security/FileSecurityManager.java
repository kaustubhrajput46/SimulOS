package com.osshell.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages file permissions.
 * Simulating a file system metadata store.
 * 
 * Permissions format: "rwx" (read, write, execute)
 * Owner: username
 */
public class FileSecurityManager {
    private static FileSecurityManager instance;
    
    // Path -> Owner
    private final Map<String, String> fileOwners;
    // Path -> Permissions (e.g., "rw-", "r--", "rwx")
    private final Map<String, String> filePermissions;

    private FileSecurityManager() {
        fileOwners = new HashMap<>();
        filePermissions = new HashMap<>();
        
        // Seed some system files
        setPermissions("system.conf", "admin", "rw-");
        setPermissions("data.txt", "user1", "rw-");
        setPermissions("readme.md", "admin", "r--");
    }

    public static synchronized FileSecurityManager getInstance() {
        if (instance == null) {
            instance = new FileSecurityManager();
        }
        return instance;
    }

    public void setPermissions(String path, String owner, String perms) {
        fileOwners.put(path, owner);
        filePermissions.put(path, perms);
    }
    
    public void createFileMetadata(String path, String owner) {
        // Default permissions for new file
        setPermissions(path, owner, "rw-");
    }

    public boolean canRead(String path, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        
        String owner = fileOwners.get(path);
        // If file not tracked, assume public read
        if (owner == null) return true;
        
        // Owner can always read
        if (owner.equals(user.getUsername())) return true;
        
        // Check "public" part of permission? 
        // For simplicity: specific file permissions
        String perms = filePermissions.get(path);
        return perms.contains("r");
    }

    public boolean canWrite(String path, User user) {
        if (user.getRole() == Role.ADMIN) return true;

        String owner = fileOwners.get(path);
        // If file not tracked, assume public write
        if (owner == null) return true;

        // Only owner or admin can write usually
        if (owner.equals(user.getUsername())) {
             String perms = filePermissions.get(path);
             return perms.contains("w");
        }
        
        return false;
    }

    public boolean canExecute(String path, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        
        String owner = fileOwners.get(path);
        if (owner == null) return true;
        
        String perms = filePermissions.get(path);
        return perms.contains("x");
    }
    
    public String getFileDetails(String path) {
        String owner = fileOwners.getOrDefault(path, "system");
        String perms = filePermissions.getOrDefault(path, "rw-");
        return String.format("%s %s", perms, owner);
    }
}

