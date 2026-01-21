package com.complexity.analyzer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
public class DynamicLoader {
    private final Object instance;
    private final Method method;

    public DynamicLoader(File classFile, String methodName) throws Exception{
        /*  Convert the classFile to URL - Local or remote Java wants the location of class which
            cant be just a single path that's why url[] array instead or sinf=gle url {url} include
            that url to container array url[]
            classFile File object can invoke getParentFile() to give Parent Class then it is converted
            to URI using toURI() and through toURL() we actually get the URL of the classFile
         */
        URL url = classFile.getParentFile().toURI().toURL();
        URL[] urls=new URL[]{url};

        // Create a class Loader Basically URLClassLoader's newInstance(Object/Container) creates a Class Object which is
        // loaded through the ClassLoader object
        ClassLoader cl= URLClassLoader.newInstance(urls);

        //Load exact class with matching name
        String className = classFile.getName().replace(".class","");
        Class<?> clazz = cl.loadClass(className);

        //Create Instance
        this.instance = clazz.getDeclaredConstructor().newInstance();

        //Find Method hardcoded for int[] for now
        this.method=clazz.getMethod(methodName,int[].class);
    }
    public void run(int[] input)throws Exception{
        method.invoke(instance, (Object) input);
    }
}
