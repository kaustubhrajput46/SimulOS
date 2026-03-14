package com.osshell.memory;

import java.util.List;

/**
 * First-In-First-Out (FIFO) page replacement algorithm.
 */
public class FIFOAlgorithm implements PageReplacementAlgorithm {
    @Override
    public int selectFrameToReplace(List<Frame> frames) {
        Frame oldestFrame = null;
        long oldestTime = Long.MAX_VALUE;

        for (Frame frame : frames) {
            if (!frame.isEmpty()) {
                if (frame.getPage().getLoadTime() < oldestTime) {
                    oldestTime = frame.getPage().getLoadTime();
                    oldestFrame = frame;
                }
            }
        }
        
        return oldestFrame != null ? oldestFrame.getFrameId() : -1;
    }
}

