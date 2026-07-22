package com.adega.adega.exception;


public class CepServiceUnavailableException extends RuntimeException{

    public CepServiceUnavailableException(String message) {
        super(message);
    }

    public CepServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
