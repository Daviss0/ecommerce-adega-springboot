package com.adega.adega.dto.checkout;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequestDTO {

    @NotNull(message = "Selecione um endereço de entrega")
    private Long addressId;

    @NotBlank
    private String checkoutToken;

    public CheckoutRequestDTO() {
    }

    public Long getAddressId() {return addressId;}

    public void setAddressId(Long addressId) {this.addressId = addressId;}

    public String getCheckoutToken() {return checkoutToken;}

    public void setCheckoutToken(String checkoutToken) {this.checkoutToken = checkoutToken;}
}
