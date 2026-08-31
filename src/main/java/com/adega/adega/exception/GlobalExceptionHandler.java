package com.adega.adega.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public String handleOrderNotFoundException(OrderNotFoundException exception,
                                                 HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("orderError", exception.getMessage());

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/")) {
            return "redirect:/admin/orders";
        }
        return "redirect:/client/orders";
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFoundException(ProductNotFoundException exception,
                                                 HttpServletRequest request,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("productError", exception.getMessage());

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin")) {
            return "redirect:/admin/products";
        }

        return "redirect:/store/homepage";
    }
}
