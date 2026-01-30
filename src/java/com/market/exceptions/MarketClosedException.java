package com.market.exceptions;

public class MarketClosedException extends Exception {

    public MarketClosedException(String message) {
        super(message);
    }

    public MarketClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}