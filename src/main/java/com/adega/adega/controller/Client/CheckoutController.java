package com.adega.adega.controller.Client;

import com.adega.adega.dto.checkout.CheckoutDTO;
import com.adega.adega.dto.checkout.CheckoutRequestDTO;
import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.entity.Order;
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
                           Model model) {

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

    @PostMapping("/client/checkout/confirm")
    public String confirmCheckout(@Valid @ModelAttribute("checkoutRequestDTO") CheckoutRequestDTO requestDTO,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  Model model) {

            if (bindingResult.hasErrors()) {
                CheckoutDTO checkout = checkoutService.getCheckout(principal.getName());
                model.addAttribute("checkout", checkout);
                return "client/checkout";
            }

            Order order = checkoutService.checkout(principal.getName(), requestDTO);

            return "redirect:/client/checkout/success/" + order.getId();

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
