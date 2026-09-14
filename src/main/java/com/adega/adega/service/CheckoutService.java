package com.adega.adega.service;


import com.adega.adega.dto.checkout.CheckoutDTO;
import com.adega.adega.dto.checkout.CheckoutRequestDTO;
import com.adega.adega.entity.Order;

public interface CheckoutService {

    CheckoutDTO getCheckout(String email);

    Order checkout(String email, CheckoutRequestDTO requestDTO);

    String createCheckoutToken(String email);
}
