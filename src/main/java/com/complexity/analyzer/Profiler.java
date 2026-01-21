package com.complexity.analyzer;

public class Profiler {
    public record Metric(int inputSize, long timeNs, long memoryBytes){};
    public static Metric analyze(DynamicLoader algo, int[] input) throws Exception{
        if (input.length<2000){
            for(int i=0;i<1000;i++) {
                int[] warmupCopy=input.clone();
                algo.run(warmupCopy);
            }
        }else{
            for(int i=0;i<50;i++) {
                int[] warmupCopy=input.clone();
                algo.run(warmupCopy);
            }
        }
        System.gc();
        Thread.sleep(5);
        Runtime rt = Runtime.getRuntime();
        long memBefore = rt.totalMemory() - rt.freeMemory();

        int iterations=2000;
        long minTime=Long.MAX_VALUE;

        for(int i=0;i<iterations;i++){
            long start=System.nanoTime();
            algo.run(input);
            long end=System.nanoTime();

            long duration=end-start;
            if(duration<minTime){
                minTime=duration;
            }
        }

        long memAfter=rt.totalMemory()-rt.freeMemory();
        long memUsed=Math.max(0, memAfter-memBefore);

        return new Metric(input.length,minTime,memUsed);
    }
}
