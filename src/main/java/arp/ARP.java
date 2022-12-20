package arp;

import arp.annotation.ProcessesClassLoader;

import java.lang.reflect.Field;

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
}
