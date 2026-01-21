package com.complexity.analyzer;

import java.lang.reflect.Method;
import java.util.concurrent.*;

public class Profiler {

    public record Metric(int inputSize, long timeNs, long memoryBytes) {}

    // Executor to run code with timeout capability
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // Inside Profiler.java

    public static Metric analyze(DynamicLoader loader, Method method, int n) throws Exception {
        // 1. Warmup (Run a few times to wake up JVM)
        Object[] warmupArgs = InputGenerator.generateArgs(method.getParameters(), n);
        for (int i = 0; i < 5; i++) loader.run(method, warmupArgs);

        // 2. Determine Iteration Count based on difficulty
        // We run ONCE to check speed.
        long startProbe = System.nanoTime();
        Object[] probeArgs = InputGenerator.generateArgs(method.getParameters(), n);
        loader.run(method, probeArgs);
        long durationProbe = System.nanoTime() - startProbe;

        // Smart Iteration Logic:
        // If it takes > 10ms, run only 5 times.
        // If it takes < 1ms, run 500 times (to smooth noise).
        int iterations;
        if (durationProbe > 10_000_000) iterations = 5;
        else if (durationProbe > 1_000_000) iterations = 50;
        else iterations = 500;

        // 3. Measure Memory
        System.gc();
        Thread.sleep(5);
        Runtime rt = Runtime.getRuntime();
        long memBefore = rt.totalMemory() - rt.freeMemory();

        // 4. Execution Loop (Min-Time Strategy)
        long minTime = Long.MAX_VALUE;

        for (int i = 0; i < iterations; i++) {
            // We must generate fresh args every time in case the algo modifies them (e.g. Sort)
            // This overhead is NOT included in the timer.
            Object[] args = InputGenerator.generateArgs(method.getParameters(), n);

            long start = System.nanoTime();
            loader.run(method, args);
            long end = System.nanoTime();

            long duration = end - start;
            if (duration < minTime) minTime = duration;
        }

        long memAfter = rt.totalMemory() - rt.freeMemory();
        long memUsed = Math.max(0, memAfter - memBefore);

        return new Metric(n, minTime, memUsed);
    }

    // Helper to run code and kill it if it freezes (e.g., waiting for Scanner input)
    private static void runWithTimeout(DynamicLoader loader, Method method, Object[] args) throws Exception {
        Future<?> future = executor.submit(() -> {
            try {
                loader.run(method, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            // Wait max 2 seconds. If it takes longer, it's likely stuck on Scanner.next()
            future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // Kill the thread
            throw new Exception("Method timed out! It might be waiting for Console Input (Scanner). Please use a method that takes Arguments instead.");
        } catch (ExecutionException e) {
            // Unwrap the actual error from the user's code
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                throw (Exception) cause.getCause();
            }
            throw new Exception(e.getMessage());
        }
    }
}