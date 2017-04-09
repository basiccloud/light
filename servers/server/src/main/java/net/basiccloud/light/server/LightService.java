package net.basiccloud.light.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * light service annotation
 */
@SuppressWarnings("unused")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface LightService {

    /**
     * service version
     * @return version
     */
    String version() default "1.0.0";

    /**
     * proto class
     * @return class
     */
    Class<?> protoClass();

    /**
     * set this service is a mock service
     * @return true or false
     */
    boolean mock() default false;
}
