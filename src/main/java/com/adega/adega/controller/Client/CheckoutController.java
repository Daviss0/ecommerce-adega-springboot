package com.adega.adega.controller.Client;

import com.adega.adega.dto.checkout.CheckoutDTO;
import com.adega.adega.dto.checkout.CheckoutRequestDTO;
import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.entity.Order;
import com.adega.adega.exception.*;
import com.adega.adega.service.CheckoutService;
import com.adega.adega.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public CheckoutController(CheckoutService checkoutService, OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @GetMapping("/client/checkout")
    public String checkout(Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {

        CheckoutDTO checkout = checkoutService.getCheckout(principal.getName());

        CheckoutRequestDTO requestDTO = new CheckoutRequestDTO();

        requestDTO.setCheckoutToken(checkoutService.createCheckoutToken(principal.getName()));

        checkout.getAddresses()
                .stream()
                .filter(AddressDTO::isPrincipal)
                .findFirst()
                .ifPresent(address ->
                        requestDTO.setAddressId(
                                address.getId()
                        )
                );

        model.addAttribute("checkout", checkout);
        model.addAttribute("checkoutRequestDTO", requestDTO);
        return "client/checkout";
        }
        catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/client/cart";
        }
        catch (AddressNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/client/account";
        }
        catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "A quantidade de um dos produtos do seu carrinho não está mais disponível.");
            return "redirect:/client/cart";
        }
        catch (ProductUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Um dos produtos do seu carrinho não está mais disponível para venda.");
            return "redirect:/client/cart";
        }
    }

    @PostMapping("/client/checkout/confirm")
    public String confirmCheckout(@Valid @ModelAttribute("checkoutRequestDTO") CheckoutRequestDTO requestDTO,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {

            if (bindingResult.hasErrors()) {
                CheckoutDTO checkout = checkoutService.getCheckout(principal.getName());
                model.addAttribute("checkout", checkout);
                return "client/checkout";
            }

            Order order = checkoutService.checkout(principal.getName(), requestDTO);

            return "redirect:/client/checkout/success/" + order.getId();
        }
        catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/client/cart";
        }
        catch (AddressNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cadastre um endereço de entrega para continuar com a compra.");
            return "redirect:/client/account";
        }
        catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "A quantidade de um dos produtos do seu carrinho não está mais disponível. Revise o carrinho antes de continuar.");
            return "redirect:/client/cart";
        }
        catch (ProductUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Um dos produtos do seu carrinho não está mais disponível para venda.");
            return "redirect:/client/cart";
        }
        catch (DuplicateCheckoutException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Este pedido já foi confirmado ou está sendo processado.");
            return "redirect:/client/cart";
        }
    }


    @GetMapping("/client/checkout/success/{orderId}")
    public String checkoutSuccess(@PathVariable Long orderId,
                                  Principal principal,
                                  Model model) {

        ClientOrderDetailsDTO order = orderService.findOrderDetailsByClientEmail(orderId, principal.getName());
        model.addAttribute("order", order);

        return "client/checkout-success";
    }
}
