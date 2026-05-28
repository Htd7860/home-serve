package com.qw.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author：qw
 * @Package：com.qw.user.annotation
 * @Project：home-serve
 * @name：RequireRole
 * @Date：2026/5/16 20:57
 * @Filename：RequireRole
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
String[] value();
}
