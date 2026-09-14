package com.adega.adega.mapper;

import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.dto.checkout.CheckoutDTO;
import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.entity.OrderItem;
import com.adega.adega.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckoutMapper {

    public CheckoutDTO toCheckoutDTO(CartDTO cartDTO, List<AddressDTO> addresses) {

        CheckoutDTO checkoutDTO = new CheckoutDTO();

        checkoutDTO.setItems(cartDTO.getItems());
        checkoutDTO.setAddresses(addresses);
        checkoutDTO.setTotal(cartDTO.getTotal());
        return checkoutDTO;
    }

    public OrderItem toOrderItem(CartItemDTO cartItem, Product product) {

        OrderItem orderItem = new OrderItem();

        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setUnitPrice(product.getPrice());
        return orderItem;
    }
}
