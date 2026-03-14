package com.osshell.memory;

/**
 * Represents a virtual page in the memory system.
 */
public class Page {
    private final int pageId;
    private final int processId;
    private long loadTime;
    private long lastAccessTime;

    public Page(int pageId, int processId) {
        this.pageId = pageId;
        this.processId = processId;
        this.loadTime = System.currentTimeMillis();
        this.lastAccessTime = this.loadTime;
    }

    public int getPageId() {
        return pageId;
    }

    public int getProcessId() {
        return processId;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void access() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "P" + processId + ":Pg" + pageId;
    }
}

