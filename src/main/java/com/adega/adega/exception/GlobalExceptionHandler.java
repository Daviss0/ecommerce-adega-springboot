package com.adega.adega.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public String handleOrderNotFoundException(
            OrderNotFoundException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/")) {
            return "redirect:/admin/orders";
        }

        return "redirect:/client/orders";
    }


    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public String handleInvalidOrderStatusTransitionException(
            InvalidOrderStatusTransitionException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/")) {
            return "redirect:/admin/orders";
        }

        return "redirect:/client/orders";
    }


    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFoundException(
            ProductNotFoundException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/stock")) {
            return "redirect:/admin/stock";
        }

        if (requestUri.startsWith("/admin")) {
            return "redirect:/admin/products";
        }

        if (requestUri.startsWith("/client/cart") || requestUri.startsWith("/client/checkout")) {
            return "redirect:/client/cart";
        }

        return "redirect:/store/homepage";
    }

    @ExceptionHandler(CartException.class)
    public String handleCartException(CartException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/client/cart";
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public String handleAddressNotFoundException(AddressNotFoundException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/client/account";
    }

    @ExceptionHandler(InsufficientStockException.class)
    public String handleInsufficientStockException(InsufficientStockException exception, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/stock")) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/stock";
        }

        redirectAttributes.addFlashAttribute("errorMessage",
                "A quantidade de um dos produtos do seu carrinho não está mais disponível. Revise o carrinho antes de continuar.");

        return "redirect:/client/cart";
    }

    @ExceptionHandler(ProductUnavailableException.class)
    public String handleProductUnavailableException(ProductUnavailableException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "Um dos produtos do seu carrinho não está mais disponível para venda.");
        return "redirect:/client/cart";
    }

    @ExceptionHandler(DuplicateCheckoutException.class)
    public String handleDuplicateCheckoutException(DuplicateCheckoutException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "Este pedido já foi confirmado ou está sendo processado.");
        return "redirect:/client/cart";
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public String handleClientNotFoundException(ClientNotFoundException exception, HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin/")) {
            return "redirect:/admin/clients";
        }

        return "redirect:/store/homepage";
    }

    @ExceptionHandler(InvalidStockQuantityException.class)
    public String handleInvalidStockQuantityException(InvalidStockQuantityException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/admin/stock";
    }

    @ExceptionHandler(ProductImageException.class)
    public String handleProductImageException(ProductImageException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/admin/products";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException exception) {
        throw exception;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request,
            HttpServletResponse response) {

        response.setStatus(exception.getStatusCode().value());

        logger.warn("Erro HTTP {} ao processar a requisição [{} {}]",
                exception.getStatusCode().value(),
                request.getMethod(),
                request.getRequestURI());

        if (exception.getStatusCode().value() == 404) {
            return "error/404";
        }

        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception exception, HttpServletRequest request) {

        logger.error("Erro inesperado ao processar a requisição [{} {}]",
                request.getMethod(),
                request.getRequestURI(),
                exception);

        return "error/500";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException exception, HttpServletRequest request) {

        logger.warn("Recurso não encontrado [{} {}]",
                request.getMethod(),
                request.getRequestURI());
        return "error/404";
    }

}