package iuh.fit.se.phazelsound.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key() default "general";

    long count() default 3;

    long period() default 180;

    TimeUnit unit() default TimeUnit.SECONDS;
}
