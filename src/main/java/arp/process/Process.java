package arp.process;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target(METHOD)
public @interface Process {

	boolean publish() default false;

	boolean dontPublishWhenResultIsNull() default false;

	String name() default "";

	String listening() default "";
}
