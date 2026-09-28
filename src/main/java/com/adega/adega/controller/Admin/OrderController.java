package com.adega.adega.controller.Admin;

import com.adega.adega.entity.Order;
import com.adega.adega.entity.OrderStatusHistory;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.exception.InvalidOrderStatusTransitionException;
import com.adega.adega.exception.OrderNotFoundException;
import com.adega.adega.repository.OrderStatusHistoryRepository;
import com.adega.adega.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderStatusHistoryRepository  orderStatusHistoryRepository;

    public OrderController(OrderService orderService, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderService = orderService;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) OrderStatus status,
                             Model model) {
        List<Order> orders;

        if(status != null) {
            orders = orderService.findByStatus(status);
        }
        else {
            orders = orderService.findAll();
        }

        Map<Long, List<OrderStatusHistory>> orderHistories = orders.stream()
                        .collect(Collectors.toMap(
                                Order::getId,
                                order -> orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId())
                        ));

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("orderHistories", orderHistories);

        return "admin/orders";
    }

   @PostMapping("/{id}/ship")
    public String markAsShipped(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        orderService.markAsShipped(id, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage",
                "Pedido marcado como saiu para entrega.");

        return "redirect:/admin/orders";
   }

   @PostMapping("/{id}/deliver")
    public String markAsDelivered(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        orderService.markAsDelivered(id, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage",
                "Entrega confirmada com sucesso.");

        return "redirect:/admin/orders";
   }

   @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        orderService.cancel(id, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage",
                "pedido cancelado com sucesso.");
        return "redirect:/admin/orders";
   }

   //metodo para tratamento de erro
    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public String handleInvalidStatusTransition(InvalidOrderStatusTransitionException ex,
                                                RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/orders";
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public String handleOrderNotFound(
            OrderNotFoundException ex,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
        );

        return "redirect:/admin/orders";
    }
}
