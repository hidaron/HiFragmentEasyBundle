package org.limlee.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入的字段
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Extra {

    String value() default "";
}
