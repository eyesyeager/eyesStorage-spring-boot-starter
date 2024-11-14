package io.github.eyesyeager.eyesStorageStarter.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author artonyu
 * @date 2024-11-13 14:31
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PutCompress {

    /**
     * source
     * @return
     */
    String value() default "";
}
