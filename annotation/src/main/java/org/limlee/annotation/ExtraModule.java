package org.limlee.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入模块
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface ExtraModule {

    String value() default "";
}
