package com.adega.adega.controller.Client;

import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/client/orders")
public class ClientOrderController {

    private final OrderService orderService;

    public ClientOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(Authentication authentication, Model model) {
        String email = authentication.getName();

        List<ClientOrderSummaryDTO> orders = orderService.findOrdersByClientEmail(email);
        model.addAttribute("orders", orders);
        return "client/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Authentication authentication, Model model) {
        String email = authentication.getName();

        ClientOrderDetailsDTO order = orderService.findOrderDetailsByClientEmail(id, email);
        model.addAttribute("order", order);
        return "client/order-details";
    }
}
