package com.osshell.memory;

import java.util.List;

/**
 * Least Recently Used (LRU) page replacement algorithm.
 */
public class LRUAlgorithm implements PageReplacementAlgorithm {
    @Override
    public int selectFrameToReplace(List<Frame> frames) {
        Frame lruFrame = null;
        long oldestAccessTime = Long.MAX_VALUE;

        for (Frame frame : frames) {
            if (!frame.isEmpty()) {
                if (frame.getPage().getLastAccessTime() < oldestAccessTime) {
                    oldestAccessTime = frame.getPage().getLastAccessTime();
                    lruFrame = frame;
                }
            }
        }
        
        return lruFrame != null ? lruFrame.getFrameId() : -1;
    }
}

