package net.basiccloud.light.server.internal;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@SuppressWarnings("unused")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface LightBuildInService {
    /**
     * protoClass
     *
     * @return protoClass
     */
    Class<?> protoClass();
}
