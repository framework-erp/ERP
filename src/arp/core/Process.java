package arp.core;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target(METHOD)
public @interface Process {
	// TODO 定义过程当中的聚合是否独立拷贝，或者这个事情是仓库的粒度？
}
