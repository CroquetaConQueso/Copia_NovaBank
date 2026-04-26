package com.novabank.exception;

public class CuentaNotFoundException extends ResourceNotFoundException {

    public CuentaNotFoundException(String message) {
        super(message);
    }
}
