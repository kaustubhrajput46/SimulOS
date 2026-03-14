package com.osshell.sync;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Validates synchronization using the Producer-Consumer problem.
 */
public class ProducerConsumerSimulation {
    private final int bufferSize;
    private final Queue<Integer> buffer;
    
    // Semaphores
    private final MySemaphore mutex;
    private final MySemaphore empty;
    private final MySemaphore full;
    
    private final List<Thread> threads;
    private volatile boolean running;

    public ProducerConsumerSimulation(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new LinkedList<>();
        
        // Mutex for exclusive access to buffer
        this.mutex = new MySemaphore(1);
        
        // Empty slots semaphore (starts with bufferSize)
        this.empty = new MySemaphore(bufferSize);
        
        // Full slots semaphore (starts with 0)
        this.full = new MySemaphore(0);
        
        this.threads = new ArrayList<>();
        this.running = false;
    }

    public void start(int producerCount, int consumerCount) {
        if (running) {
            System.out.println("Simulation already running.");
            return;
        }
        
        running = true;
        System.out.println("Starting Producer-Consumer Simulation...");
        System.out.println("Buffer Size: " + bufferSize);
        System.out.println("Producers: " + producerCount + ", Consumers: " + consumerCount);
        
        for (int i = 0; i < producerCount; i++) {
            Thread t = new Thread(new Producer(i + 1));
            t.setName("Producer-" + (i + 1));
            threads.add(t);
            t.start();
        }
        
        for (int i = 0; i < consumerCount; i++) {
            Thread t = new Thread(new Consumer(i + 1));
            t.setName("Consumer-" + (i + 1));
            threads.add(t);
            t.start();
        }
    }

    public void stop() {
        if (!running) return;
        
        System.out.println("Stopping simulation...");
        running = false;
        
        for (Thread t : threads) {
            t.interrupt();
        }
        
        threads.clear();
        buffer.clear();
        System.out.println("Simulation stopped.");
    }

    private class Producer implements Runnable {
        private final int id;

        public Producer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                int item = 0;
                while (running) {
                    item++;
                    
                    // Wait for empty slot
                    empty.acquire();
                    
                    // Lock buffer
                    mutex.acquire();
                    
                    if (running) { // Double check
                        buffer.add(item);
                        System.out.println("[Producer " + id + "] Produced item " + item + " (Buffer: " + buffer.size() + "/" + bufferSize + ")");
                    }
                    
                    // Unlock buffer
                    mutex.release();
                    
                    // Signal full slot
                    full.release();
                    
                    Thread.sleep((long) (Math.random() * 1000) + 500); // Simulate work
                }
            } catch (InterruptedException e) {
                // Thread interrupted, exit gracefully
            }
        }
    }

    private class Consumer implements Runnable {
        private final int id;

        public Consumer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    // Wait for full slot
                    full.acquire();
                    
                    // Lock buffer
                    mutex.acquire();
                    
                    int item = -1;
                    if (running && !buffer.isEmpty()) {
                        item = buffer.poll();
                        System.out.println("[Consumer " + id + "] Consumed item " + item + " (Buffer: " + buffer.size() + "/" + bufferSize + ")");
                    }
                    
                    // Unlock buffer
                    mutex.release();
                    
                    // Signal empty slot
                    empty.release();
                    
                    if (item != -1) {
                        Thread.sleep((long) (Math.random() * 1000) + 500); // Simulate consumption
                    }
                }
            } catch (InterruptedException e) {
                // Thread interrupted
            }
        }
    }
}

