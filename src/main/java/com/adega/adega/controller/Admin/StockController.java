package com.adega.adega.controller.Admin;


import com.adega.adega.entity.Order;
import com.adega.adega.service.StockService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public String stockPage(Model model) {
        model.addAttribute("products", stockService.listProductsStock());
        return "admin/stock";
    }

    @PostMapping("/add/{id}")
    public String addStock(@PathVariable Long id,
                            @RequestParam Integer quantity,
                            @RequestParam String reason,
                            Authentication authentication,
                           RedirectAttributes redirectAttributes) {


            stockService.addStock(id, quantity, reason, authentication.getName());

            redirectAttributes.addAttribute("successMessage", "Entrada de estoque realizada com sucesso.");

        return "redirect:/admin/stock";
    }

    @PostMapping("/remove/{id}")
    public String removeStock(@PathVariable Long id,
                               @RequestParam Integer quantity,
                               @RequestParam String reason,
                               Authentication authentication,
                              RedirectAttributes redirectAttributes) {

            stockService.removeStock(id, quantity, reason, authentication.getName());

            redirectAttributes.addFlashAttribute("success", "Saída de estoque realizada com sucesso.");

        return "redirect:/admin/stock";
    }

    @GetMapping("/movements")
    public String movementsPage(Model model) {
        model.addAttribute("movements", stockService.listMovements());
        return "admin/stock_movements";
    }
}
