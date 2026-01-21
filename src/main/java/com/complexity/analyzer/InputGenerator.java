package com.complexity.analyzer;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InputGenerator {
    private static final Random rand = new Random();

    // Generates inputs for a specific method and size N
    public static Object[] generateArgs(Parameter[] params, int n) {
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            args[i] = createValue(type, n);
        }
        return args;
    }

    private static Object createValue(Class<?> type, int n) {
        // 1. Arrays (The Driver for N)
        if (type == int[].class) return generateIntArray(n);
        if (type == double[].class) return generateDoubleArray(n);
        if (type == String[].class) return generateStringArray(n);

        // 2. Collections
        if (List.class.isAssignableFrom(type)) return generateIntList(n);

        // 3. Primitives & Objects (Auxiliary inputs)
        // We usually keep these small or random, not necessarily scaling with N
        if (type == int.class || type == Integer.class) return rand.nextInt(n); // Random number
        if (type == double.class || type == Double.class) return rand.nextDouble();
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == String.class) return generateString(n); // String algos scale with length

        return null; // Unsupported types
    }

    // --- Data Generators ---
    private static int[] generateIntArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rand.nextInt(100000);
        // CRITICAL: Sort the array so Binary Search works!
        // This doesn't hurt Linear Search (it just searches a sorted array).
        java.util.Arrays.sort(arr);
        return arr;
    }

    private static double[] generateDoubleArray(int n) {
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) arr[i] = rand.nextDouble();
        return arr;
    }

    private static String[] generateStringArray(int n) {
        String[] arr = new String[n];
        for(int i=0; i<n; i++) arr[i] = "Str" + i;
        return arr;
    }

    private static List<Integer> generateIntList(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(rand.nextInt(10000));
        return list;
    }

    private static String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rand.nextInt(26)));
        }
        return sb.toString();
    }
}
