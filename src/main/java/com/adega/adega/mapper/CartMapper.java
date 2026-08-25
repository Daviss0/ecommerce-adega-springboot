package com.adega.adega.mapper;

import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.entity.Cart;
import com.adega.adega.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if(cart == null){
            return null;
        }

        return new CartDTO(
                cart.getId(),
                toItemDTOList(cart.getItems()),
                cart.getTotal()
        );
    }

    public CartItemDTO toItemDTO(CartItem cartItem) {
        if(cartItem == null) {
            return null;
        }

        return new CartItemDTO(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice(),
                cartItem.getSubtotal()
        );
    }

    public List<CartItemDTO> toItemDTOList(List<CartItem> items) {

        if (items ==null) {
            return List.of();
        }
        return items.stream().map(this::toItemDTO).toList();
    }
}
