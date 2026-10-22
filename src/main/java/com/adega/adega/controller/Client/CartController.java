package com.adega.adega.controller.Client;


import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.adega.adega.util.ProductCategories;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import java.util.Map;

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

            cartService.addProduct(authentication.getName(), addCartDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Produto adicionado ao carrinho.");


        return "redirect:/store/homepage";
    }

    @PostMapping("/items/{id}/increase")
    public String increaseItem(@PathVariable Long id, Authentication authentication) {

            cartService.increaseQuantity(authentication.getName(), id);

        return "redirect:/client/cart";
    }

    @PostMapping("/items/{id}/decrease")
    public String decreaseItem(@PathVariable Long id, Authentication authentication) {

            cartService.decreaseQuantity(authentication.getName(), id);

        return "redirect:/client/cart";
    }

    @PostMapping("/items/{id}/remove")
    public String deleteItem(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {


            cartService.removeItem(authentication.getName(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto removido do carrinho.");

        return "redirect:/client/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {

            cartService.clearCart(authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Carrinho esvaziado.");

        return "redirect:/client/cart";
    }

    @PostMapping("/products/{productId}/increase")
    public String increaseProductFromStore(
            @PathVariable Long productId,
            @RequestParam(required = false) String categorySlug,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        AddCartDTO dto = new AddCartDTO();
        dto.setProductId(productId);
        dto.setQuantity(1);

        cartService.addProduct(authentication.getName(), dto);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Quantidade atualizada no carrinho."
        );

        return storeRedirect(categorySlug);
    }

    @PostMapping("/products/{productId}/decrease")
    public String decreaseProductFromStore(
            @PathVariable Long productId,
            @RequestParam(required = false) String categorySlug,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CartDTO cart = cartService.getCart(authentication.getName());

        CartItemDTO item = cart.getItems().stream()
                .filter(cartItem -> productId.equals(cartItem.getProductId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return storeRedirect(categorySlug);
        }

        if (item.getQuantity() > 1) {
            cartService.decreaseQuantity(
                    authentication.getName(),
                    item.getId()
            );
        } else {
            cartService.removeItem(
                    authentication.getName(),
                    item.getId()
            );
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Quantidade atualizada no carrinho."
        );

        return storeRedirect(categorySlug);
    }

    private String storeRedirect(String categorySlug) {

        if (categorySlug != null && ProductCategories.fromSlug(categorySlug).isPresent()) {
            return "redirect:/store/category/" + categorySlug;
        }
        return "redirect:/store/homepage";
    }

    @PostMapping("/ajax/products/{productId}/increase")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> increaseProductAjax(@PathVariable Long productId,
                                                                  Authentication authentication) {

        AddCartDTO dto = new AddCartDTO();
        dto.setProductId(productId);
        dto.setQuantity(1);

        cartService.addProduct(authentication.getName(), dto);

        CartDTO cart = cartService.getCart(authentication.getName());

        int quantity = cart.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .mapToInt(CartItemDTO::getQuantity)
                .findFirst()
                .orElse(0);

        return ResponseEntity.ok(Map.of("productId", productId, "quantity", quantity,
                                        "cartItemCount", cartService.getTotalItems(authentication.getName()),
                                            "cartTotal", cartService.getCartTotal(authentication.getName())));
    }

    @PostMapping("/ajax/products/{productId}/decrease")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> decreaseProductAjax(@PathVariable Long productId,
                                                                   Authentication authentication) {

        CartDTO cart = cartService.getCart(authentication.getName());

        CartItemDTO item = cart.getItems().stream()
                .filter(cartItem -> productId.equals(cartItem.getProductId()))
                .findFirst()
                .orElse(null);

        if (item != null) {

            if (item.getQuantity() > 1) {
                cartService.decreaseQuantity(authentication.getName(), item.getId());
            } else {
                cartService.removeItem(authentication.getName(), item.getId());
            }
        }
            CartDTO updatedCart = cartService.getCart(authentication.getName());

        int quantity = updatedCart.getItems().stream()
                .filter(cartItem -> productId.equals(cartItem.getProductId()))
                .mapToInt(CartItemDTO::getQuantity)
                .findFirst()
                .orElse(0);

            return ResponseEntity.ok(Map.of("productId", productId, "quantity", quantity,
                    "cartItemCount", cartService.getTotalItems(authentication.getName()),
                    "cartTotal", cartService.getCartTotal(authentication.getName())));
        }


}
