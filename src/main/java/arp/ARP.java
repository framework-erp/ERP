package arp;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;
import arp.enhance.ProcessesClassLoader;

import java.lang.reflect.Field;

public class ARP {
    public static void start(String... pkgs) throws Exception {
        ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
    }

    public static void useAnnotation() {
        ClassLoader currLoader = Thread.currentThread().getContextClassLoader();
        Class clazz = currLoader.getClass();
        Field parentField = null;
        while (clazz != null) {
            try {
                parentField = clazz.getDeclaredField("parent");
                parentField.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                continue;
            }
        }
        Object parent = null;
        try {
            parent = parentField.get(currLoader);
            parentField.set(currLoader, new ProcessesClassLoader((ClassLoader) parent));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
