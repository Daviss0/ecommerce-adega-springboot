package com.adega.adega.controller.Client;

import com.adega.adega.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@ControllerAdvice(basePackages = "com.adega.adega.controller.Client")
public class ClientNavbarControllerAdvice {

    private final CartService cartService;

    public ClientNavbarControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute
    public void addCartNavbarData(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("cartItemCount", 0);
            model.addAttribute("cartTotal", BigDecimal.ZERO);
            return;
        }

        boolean isClient = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT"));

        if (!isClient) {
            model.addAttribute("cartItemCount", 0);
            model.addAttribute("cartTotal", BigDecimal.ZERO);
            return;
        }

      String email = authentication.getName();

        model.addAttribute("cartItemCount", cartService.getTotalItems(email));
        model.addAttribute("cartTotal", cartService.getCartTotal(email));
    }


}
