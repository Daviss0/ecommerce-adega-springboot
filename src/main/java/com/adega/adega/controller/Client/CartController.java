package com.adega.adega.controller.Client;


import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/client/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(Authentication authentication, Model model) {

        CartDTO cart = cartService.getCart(authentication.getName());

        model.addAttribute("cart", cart);
        return "client/cart";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute ("addCartDTO")AddCartDTO addCartDTO,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não foi possível adicionar o produto ao carrinho.");
            return "redirect:/store/homepage";
        }

        try {
            cartService.addProduct(authentication.getName(), addCartDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Produto adicionado ao carrinho.");
        }
        catch(RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/store/homepage";
    }

    @PostMapping("/items/{id}/increase")
    public String increaseItem(@PathVariable Long id, Authentication authentication,RedirectAttributes redirectAttributes) {

        try {
            cartService.increaseQuantity(authentication.getName(), id);
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/client/cart";
    }

    @PostMapping("/items/{id}/decrease")
    public String decreaseItem(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            cartService.decreaseQuantity(authentication.getName(), id);
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/client/cart";
    }

    @PostMapping("/items/{id}/remove")
    public String deleteItem(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {

        try {
            cartService.removeItem(authentication.getName(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto removido do carrinho.");
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/client/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart(authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Carrinho esvaziado.");
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/client/cart";
    }

}
