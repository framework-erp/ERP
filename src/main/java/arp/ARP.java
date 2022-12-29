package arp;

import arp.annotation.ProcessesClassLoader;
import arp.process.ProcessWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class ARP {
    public static void useAnnotation() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class clc = cl.getClass();
        Field parentField = null;
        while (clc != null) {
            try {
                parentField = clc.getDeclaredField("parent");
                parentField.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clc = clc.getSuperclass();
                continue;
            }
        }
        Object parent = null;
        try {
            parent = parentField.get(cl);
            parentField.set(cl, new ProcessesClassLoader((ClassLoader) parent));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static<V> V go(String processName, Callable<V> process) {
        ProcessWrapper.beforeProcessStart(processName);
        try {
            V v = process.call();
            ProcessWrapper.afterProcessFinish();
            return v;
        } catch (Exception e) {
            ProcessWrapper.afterProcessFailed();
            throw new RuntimeException(e);
        }
    }
}
