package com.inventoryAPIs.demo.exception;

/** A valid request that would break a business rule, e.g. removing the last active admin. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
