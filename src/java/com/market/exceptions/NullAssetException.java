package com.market.exceptions;

public class NullAssetException extends IllegalArgumentException {
    public NullAssetException(String message) {
        super(message);
    }
}
