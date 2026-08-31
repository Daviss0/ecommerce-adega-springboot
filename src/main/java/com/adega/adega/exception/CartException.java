package com.adega.adega.exception;

import com.adega.adega.entity.Cart;

public class CartException extends RuntimeException{

    public CartException(String message) {
        super(message);
    }
}
