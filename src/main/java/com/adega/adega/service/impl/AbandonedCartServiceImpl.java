package com.adega.adega.service.impl;

import com.adega.adega.entity.Cart;
import com.adega.adega.repository.CartRepository;
import com.adega.adega.service.AbandonedCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AbandonedCartServiceImpl implements AbandonedCartService {

    private final CartRepository cartRepository;
    private final long abandonedCartDays;

    public AbandonedCartServiceImpl(CartRepository cartRepository,
                                    @Value("${app.cart.abandoned-days}") long abandonedCartDays) {
        this.cartRepository = cartRepository;
        this.abandonedCartDays = abandonedCartDays;
    }

    @Override
    @Transactional
    public int clearAbandonedCarts() {

        LocalDateTime limitDate =
                LocalDateTime.now().minusDays(abandonedCartDays);

        List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(limitDate);

        for (Cart cart : abandonedCarts) {
            cart.getItems().clear();
        }
        return abandonedCarts.size();
    }
}
