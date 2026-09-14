package com.adega.adega.exception;

public class DuplicateCheckoutException extends RuntimeException{

    public DuplicateCheckoutException(String message){
        super(message);
    }
}
