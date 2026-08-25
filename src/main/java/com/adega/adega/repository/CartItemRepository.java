package com.adega.adega.repository;

import com.adega.adega.entity.Cart;
import com.adega.adega.entity.CartItem;
import com.adega.adega.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);
}
