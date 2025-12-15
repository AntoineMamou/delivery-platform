package com.delivery.core.managers.exceptions;

public class InvalidDeliveryWindowException extends Exception {

    public InvalidDeliveryWindowException(String message) {
        super(message);
    }

    public InvalidDeliveryWindowException(String message, Throwable cause) {
        super(message, cause);
    }
}
