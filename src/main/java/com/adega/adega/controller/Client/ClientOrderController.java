package com.adega.adega.controller.Client;

import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.entity.OrderStatusHistory;
import com.adega.adega.exception.OrderNotFoundException;
import com.adega.adega.repository.OrderStatusHistoryRepository;
import com.adega.adega.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/client/orders")
public class ClientOrderController {

    private final OrderService orderService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public ClientOrderController(OrderService orderService, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderService = orderService;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;

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

        List<OrderStatusHistory> history = orderStatusHistoryRepository
                .findByOrderIdOrderByChangedAtAsc(id);

        model.addAttribute("order", order);
        model.addAttribute("history", history);
        return "client/order-details";
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public String orderNotFound(OrderNotFoundException ex,
                                RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:/client/orders";
    }
}
