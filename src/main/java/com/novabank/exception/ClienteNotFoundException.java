package com.novabank.exception;

public class ClienteNotFoundException extends ResourceNotFoundException {

    public ClienteNotFoundException(String message) {
        super(message);
    }
}
