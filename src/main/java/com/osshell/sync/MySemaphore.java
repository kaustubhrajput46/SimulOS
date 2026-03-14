package com.osshell.sync;

/**
 * A custom implementation of a counting semaphore.
 */
public class MySemaphore {
    private int permits;
    private final int maxPermits;

    public MySemaphore(int permits) {
        this.permits = permits;
        this.maxPermits = Integer.MAX_VALUE;
    }

    public MySemaphore(int permits, int maxPermits) {
        this.permits = permits;
        this.maxPermits = maxPermits;
    }

    /**
     * Acquires a permit from the semaphore.
     * Blocks if no permits are available.
     */
    public synchronized void acquire() throws InterruptedException {
        while (permits <= 0) {
            wait();
        }
        permits--;
    }

    /**
     * Releases a permit to the semaphore.
     */
    public synchronized void release() {
        if (permits < maxPermits) {
            permits++;
            notifyAll();
        }
    }
    
    public synchronized int availablePermits() {
        return permits;
    }
}

