package com.adega.adega.dto.checkout;


import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.dto.client.AddressDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckoutDTO {

    private List<CartItemDTO> items = new ArrayList<>();
    private List<AddressDTO> addresses = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    public CheckoutDTO() {
    }

    public CheckoutDTO(List<CartItemDTO> items, List<AddressDTO> addresses, BigDecimal total) {
        this.items = items;
        this.addresses = addresses;
        this.total = total;
    }

    public List<CartItemDTO> getItems() {return items;}

    public void setItems(List<CartItemDTO> items) {this.items = items;}

    public List<AddressDTO> getAddresses() {return addresses;}

    public void setAddresses(List<AddressDTO> addresses) {this.addresses = addresses;}

    public BigDecimal getTotal() {return total;}

    public void setTotal(BigDecimal total) {this.total = total;}
}
