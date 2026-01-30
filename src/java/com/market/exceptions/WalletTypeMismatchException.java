package com.market.exceptions;

public class WalletTypeMismatchException extends Exception {

    public WalletTypeMismatchException(String message) {
        super(message);
    }

    public WalletTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}