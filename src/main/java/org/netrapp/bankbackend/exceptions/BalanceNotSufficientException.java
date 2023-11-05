package org.netrapp.bankbackend.exceptions;

public class BalanceNotSufficientException extends Exception {

    public BalanceNotSufficientException(String message) {
        super(message);
    }
}
