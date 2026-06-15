package com.adega.adega.controller;

import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);

        return "orders";
    }

    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);

        return "redirect:/admin/orders";

    }
}
