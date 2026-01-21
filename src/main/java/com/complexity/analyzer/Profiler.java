package com.complexity.analyzer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Profiler {

    // Record to hold the results of a single analysis step
    public record Metric(int inputSize, long timeNs, long memoryBytes) {}

    /**
     * Runs the analysis for a specific input size (n).
     * Uses a "Time Budget" strategy: Run the algorithm repeatedly for up to 50ms.
     * This ensures fast algos get smooth graphs, and slow algos don't freeze the app.
     */
    public static Metric analyze(DynamicLoader loader, Method method, int n) throws Exception {
        // 1. Generate Master Input Data (Do this ONCE to save time)
        // We will copy this data for every run so the algorithm doesn't destroy it.
        Object[] masterArgs = InputGenerator.generateArgs(method.getParameters(), n);

        // 2. Warmup Phase
        // Run a few times to wake up the JVM (JIT Compiler), ignoring the timing.
        for (int i = 0; i < 5; i++) {
            loader.run(method, copyArgs(masterArgs));
        }

        // 3. Memory Measurement (Before)
        System.gc(); // Request Garbage Collection
        // No Thread.sleep() needed here if we want to keep things snappy
        Runtime rt = Runtime.getRuntime();
        long memBefore = rt.totalMemory() - rt.freeMemory();

        // 4. Execution Loop (Time-Budgeted)
        long totalTime = 0;
        int iterations = 0;

        // We allocate a "Budget" of 50 milliseconds (50,000,000 ns) for this data point.
        // Fast algos (Binary Search) will run thousands of times.
        // Slow algos (Bubble Sort) will run only once or twice.
        long maxWallClockTime = 50_000_000;
        long loopStartTime = System.nanoTime();

        while (true) {
            // Check Budget: Have we spent more than 50ms in real world time?
            if (System.nanoTime() - loopStartTime > maxWallClockTime) {
                // Ensure we ran at least once before stopping
                if (iterations > 0) break;
            }

            // Create fresh arguments (Fast Copy)
            // Crucial: ensures side-effects (like sorting) don't affect the next run.
            Object[] currentArgs = copyArgs(masterArgs);

            long start = System.nanoTime();
            loader.run(method, currentArgs);
            long end = System.nanoTime();

            totalTime += (end - start);
            iterations++;
        }

        // 5. Memory Measurement (After)
        long memAfter = rt.totalMemory() - rt.freeMemory();
        long memUsed = Math.max(0, memAfter - memBefore);

        // 6. Calculate Average Time
        long avgTime = totalTime / iterations;

        return new Metric(n, avgTime, memUsed);
    }

    /**
     * Helper: Quickly creates a deep copy of the arguments.
     * This uses native System.arraycopy for maximum speed.
     */
    private static Object[] copyArgs(Object[] args) {
        Object[] copy = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            Object original = args[i];

            if (original instanceof int[]) {
                // Fast Native Array Copy for int[]
                int[] src = (int[]) original;
                int[] dest = new int[src.length];
                System.arraycopy(src, 0, dest, 0, src.length);
                copy[i] = dest;

            } else if (original instanceof double[]) {
                // Fast Native Array Copy for double[]
                double[] src = (double[]) original;
                double[] dest = new double[src.length];
                System.arraycopy(src, 0, dest, 0, src.length);
                copy[i] = dest;

            } else if (original instanceof String[]) {
                // Copy String array
                String[] src = (String[]) original;
                String[] dest = new String[src.length];
                System.arraycopy(src, 0, dest, 0, src.length);
                copy[i] = dest;

            } else if (original instanceof List) {
                // Copy List (assuming ArrayList for simplicity)
                copy[i] = new ArrayList<>((List<?>) original);

            } else {
                // For Primitives (Integer, Double) and Strings,
                // they are immutable, so we can just pass the reference.
                copy[i] = original;
            }
        }
        return copy;
    }
}