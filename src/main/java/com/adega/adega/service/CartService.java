package com.adega.adega.service;


import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.dto.cart.CartDTO;

public interface CartService {

    CartDTO getCart(String email);

    void addProduct(String email, AddCartDTO dto);

    void increaseQuantity(String email, Long cartItemId);

    void decreaseQuantity(String email, Long cartItemId);

    void removeItem(String email, Long cartItemId);

    void clearCart(String email);

    int getTotalItems(String email);
}
