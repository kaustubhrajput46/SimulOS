package com.osshell.memory;

import java.util.List;

/**
 * Strategy interface for page replacement algorithms.
 */
public interface PageReplacementAlgorithm {
    /**
     * Selects a frame to replace.
     * @param frames List of physical memory frames.
     * @return The frameId of the frame to replace.
     */
    int selectFrameToReplace(List<Frame> frames);
}

