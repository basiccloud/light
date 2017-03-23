package net.basiccloud.light.server.core.internal;

/**
 * light runtime exception
 */
public class LightRuntimeException extends RuntimeException {


    public LightRuntimeException(String message) {
        super(message);
    }

    public LightRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
