package com.xuwei.serviceproxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/*
 * @author xuwei.luo
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface BaseUrl {
    String value() default "";
}
