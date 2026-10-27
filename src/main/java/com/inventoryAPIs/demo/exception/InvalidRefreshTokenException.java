package com.inventoryAPIs.demo.exception;

/** A refresh token that is unknown, expired, revoked or reused; the client must log in again. */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
