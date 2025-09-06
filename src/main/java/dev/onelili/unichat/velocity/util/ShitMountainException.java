package dev.onelili.unichat.velocity.util;

public class ShitMountainException extends RuntimeException {
    public ShitMountainException(String message, Exception e) {
        super(message, e);
    }
    public ShitMountainException(String message) {
        super(message);
    }
}
