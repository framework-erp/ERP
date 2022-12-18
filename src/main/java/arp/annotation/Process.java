package arp.annotation;

import arp.enhance.ProcessInfo;
import arp.enhance.ProcessesClassLoader;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target(METHOD)
public @interface Process {
}
