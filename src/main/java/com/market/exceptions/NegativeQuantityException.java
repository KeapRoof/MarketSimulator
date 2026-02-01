package com.market.exceptions;

public class NegativeQuantityException extends IllegalArgumentException {
    public NegativeQuantityException(String message) {
        super(message);
    }
}
