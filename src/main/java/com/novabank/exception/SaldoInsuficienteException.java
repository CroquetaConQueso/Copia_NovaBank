package com.novabank.exception;

public class SaldoInsuficienteException extends InsufficientBalanceException {

    public SaldoInsuficienteException(String message) {
        super(message);
    }
}
