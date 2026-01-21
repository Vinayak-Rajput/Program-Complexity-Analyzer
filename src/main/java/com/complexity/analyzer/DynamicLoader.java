package com.complexity.analyzer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DynamicLoader {
    private Object instance;
    private Class<?> clazz;

    public DynamicLoader(File classFile) throws Exception {
        // Strategy: Try loading from the immediate folder.
        // If it fails with "wrong name", move one folder up and try "Folder.Class".
        // Repeat until loaded or we hit the root.

        String definedClassName = null;
        ClassLoader cl = null;

        // 1. Initial Attempt: Assume default package
        try {
            definedClassName = classFile.getName().replace(".class", "");
            cl = getLoaderForPath(classFile.getParentFile());
            this.clazz = cl.loadClass(definedClassName);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            // 2. Package Detection Strategy
            // The error message usually contains the correct name like "wrong name: com/sorting/MySort"
            // We can parse this, OR we can brute-force walk up the directory.

            boolean loaded = false;
            File currentRoot = classFile.getParentFile();
            String currentPackage = "";

            // Try walking up 5 levels max
            for(int i=0; i<5; i++) {
                if(currentRoot.getParentFile() == null) break;

                // Move up one level
                currentPackage = currentRoot.getName() + "." + currentPackage;
                currentRoot = currentRoot.getParentFile();

                try {
                    String fullClassName = currentPackage + definedClassName;
                    cl = getLoaderForPath(currentRoot);
                    this.clazz = cl.loadClass(fullClassName);
                    loaded = true;
                    break; // Success!
                } catch (Throwable ignored) {
                    // Continue searching up
                }
            }

            if (!loaded) throw new Exception("Could not determine package structure. Please load the class from its root project folder.");
        }

        // Create Instance (Handle missing no-args constructor)
        try {
            this.instance = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // If no empty constructor, try to find ANY constructor and pass nulls/zeros
            // (Advanced feature, usually not needed for simple algos)
            throw new Exception("Class must have an empty constructor (public MyClass() {}).");
        }
    }

    private ClassLoader getLoaderForPath(File path) throws Exception {
        return URLClassLoader.newInstance(new URL[]{path.toURI().toURL()});
    }

    public List<Method> getMethods() {
        List<Method> validMethods = new ArrayList<>();
        // Get all methods, including private ones
        for (Method m : clazz.getDeclaredMethods()) {
            // Filter out Object methods (toString, wait, etc.) and compiler artifacts
            if (m.getDeclaringClass() != Object.class && !m.isSynthetic() && !m.getName().contains("$")) {
                validMethods.add(m);
            }
        }
        return validMethods;
    }

    public void run(Method method, Object[] args) throws Exception {
        method.setAccessible(true);
        method.invoke(instance, args);
    }
}