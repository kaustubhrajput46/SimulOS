package com.osshell.memory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages memory allocation, deallocation, and paging algorithms.
 */
public class MemoryManager {
    private static MemoryManager instance;

    private int totalMemorySize;
    private int pageSize;
    private int frameCount;
    private List<Frame> frames;
    private PageReplacementAlgorithm replacementAlgorithm;
    
    // Tracks allocated pages per process: ProcessID -> List of Logic Page IDs
    private Map<Integer, Integer> processMemoryRequirements; 
    
    // Page Table: ProcessID -> (PageID -> FrameID)
    // Using a nested map to simulate page table. 
    // If entry exists, page is in memory (Frame). If not, page fault.
    private Map<Integer, Map<Integer, Integer>> pageTables;
    
    // Metrics
    private Map<Integer, Integer> pageFaults;

    private MemoryManager() {
        this.processMemoryRequirements = new ConcurrentHashMap<>();
        this.pageTables = new ConcurrentHashMap<>();
        this.pageFaults = new ConcurrentHashMap<>();
        
        // Default initialization
        init(256, 16); // 256KB total, 16KB pages -> 16 frames
        setAlgorithm(new FIFOAlgorithm());
    }

    public static synchronized MemoryManager getInstance() {
        if (instance == null) {
            instance = new MemoryManager();
        }
        return instance;
    }

    public void init(int totalSize, int pgSize) {
        this.totalMemorySize = totalSize;
        this.pageSize = pgSize;
        this.frameCount = totalSize / pgSize;
        this.frames = new ArrayList<>(frameCount);
        
        for (int i = 0; i < frameCount; i++) {
            frames.add(new Frame(i));
        }
        
        // Reset state
        this.processMemoryRequirements.clear();
        this.pageTables.clear();
        this.pageFaults.clear();
    }

    public void setAlgorithm(PageReplacementAlgorithm algorithm) {
        this.replacementAlgorithm = algorithm;
    }

    /**
     * Allocates virtual memory for a process.
     * Does NOT load pages into physical memory (demand paging).
     */
    public void allocateMemory(int processId, int size) {
        processMemoryRequirements.put(processId, size);
        pageTables.put(processId, new HashMap<>());
        pageFaults.put(processId, 0);
        System.out.println("Allocated " + size + "KB virtual memory for Process " + processId);
    }

    /**
     * Deallocates memory for a process.
     */
    public void deallocateMemory(int processId) {
        // Free frames occupied by this process
        Map<Integer, Integer> table = pageTables.get(processId);
        if (table != null) {
            for (Integer frameId : table.values()) {
                frames.get(frameId).setPage(null);
            }
        }
        
        processMemoryRequirements.remove(processId);
        pageTables.remove(processId);
        pageFaults.remove(processId);
        System.out.println("Deallocated memory for Process " + processId);
    }

    /**
     * Accesses a specific virtual address for a process.
     * Triggers page fault if page not in memory.
     * @return true if page fault occurred, false otherwise
     */
    public boolean accessMemory(int processId, int virtualAddress) {
        if (!processMemoryRequirements.containsKey(processId)) {
            System.out.println("Error: Process " + processId + " has no allocated memory.");
            return false;
        }

        int maxMemory = processMemoryRequirements.get(processId);
        if (virtualAddress < 0 || virtualAddress >= maxMemory) {
            System.out.println("Error: Segmentation Fault. Accessing " + virtualAddress + " outside limit " + maxMemory);
            return false;
        }

        int pageId = virtualAddress / pageSize;
        Map<Integer, Integer> table = pageTables.get(processId);

        if (table.containsKey(pageId)) {
            // HIT: Page is in memory
            int frameId = table.get(pageId);
            Frame frame = frames.get(frameId);
            frame.getPage().access(); // Update access time (for LRU)
            System.out.println("Memory Access: Process " + processId + ", Page " + pageId + " is in Frame " + frameId + " (HIT)");
            return false;
        } else {
            // MISS: Page Fault
            handlePageFault(processId, pageId);
            return true;
        }
    }

    private void handlePageFault(int processId, int pageId) {
        System.out.println("Page Fault: Process " + processId + ", Page " + pageId + " not in memory.");
        
        // Track fault
        pageFaults.put(processId, pageFaults.getOrDefault(processId, 0) + 1);

        // Find free frame
        Frame targetFrame = null;
        for (Frame f : frames) {
            if (f.isEmpty()) {
                targetFrame = f;
                break;
            }
        }

        if (targetFrame == null) {
            // Memory is full, invoke replacement algorithm
            int frameIdToReplace = replacementAlgorithm.selectFrameToReplace(frames);
            targetFrame = frames.get(frameIdToReplace);
            Page evictedPage = targetFrame.getPage();
            
            // Remove evicted page from its owner's page table
            Map<Integer, Integer> victimTable = pageTables.get(evictedPage.getProcessId());
            if (victimTable != null) {
                victimTable.remove(evictedPage.getPageId());
            }
            
            System.out.println("Memory Full: Replaced Frame " + targetFrame.getFrameId() + " (was " + evictedPage + ")");
        } else {
            System.out.println("Loaded into free Frame " + targetFrame.getFrameId());
        }

        // Load new page into frame
        Page newPage = new Page(pageId, processId);
        targetFrame.setPage(newPage);
        
        // Update page table
        pageTables.get(processId).put(pageId, targetFrame.getFrameId());
    }

    public String getMemoryStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory Status [Total: ").append(totalMemorySize)
          .append("KB, Page Size: ").append(pageSize).append("KB")
          .append(", Frames: ").append(frameCount).append("]\n");
          
        sb.append("Algorithm: ").append(replacementAlgorithm.getClass().getSimpleName()).append("\n");
        
        sb.append("Physical Frames:\n");
        for (Frame f : frames) {
            sb.append(String.format("  Frame %2d: %s\n", 
                f.getFrameId(), 
                f.isEmpty() ? "[Free]" : f.getPage().toString()));
        }

        sb.append("\nProcess Page Tables (Active Pages):\n");
        for (Integer pid : pageTables.keySet()) {
            sb.append("  Process ").append(pid).append(": ");
            Map<Integer, Integer> table = pageTables.get(pid);
            if (table.isEmpty()) sb.append("No pages in memory");
            else {
                for (Map.Entry<Integer, Integer> entry : table.entrySet()) {
                    sb.append("[Pg").append(entry.getKey()).append("->Fr").append(entry.getValue()).append("] ");
                }
            }
            sb.append("\n");
        }

        sb.append("\nPage Fault Statistics:\n");
        for (Map.Entry<Integer, Integer> entry : pageFaults.entrySet()) {
            sb.append("  Process ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" faults\n");
        }

        return sb.toString();
    }
}


