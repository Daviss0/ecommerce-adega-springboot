package com.adega.adega.controller;

import com.adega.adega.controller.Client.StoreController;
import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;

import com.adega.adega.service.CartService;
import com.adega.adega.controller.Client.CartController;


import com.adega.adega.service.ProductService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.*;

public class StoreControllerTest {

    private ProductRepository productRepository;
    private StoreController storeController;
    private Model model;
    private CartService cartService;
    private ProductService productService;


    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        model = mock(Model.class);
        cartService = mock(CartService.class);

        storeController = new StoreController(productRepository,cartService, productService);
    }

    @Test
    void deveCarregarCategoriaValida() {

        Product cerveja = new Product();
        cerveja.setName("Cerveja Heineken");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);

        List<Product> produtos = List.of(cerveja);

        when(productRepository.findByActiveTrueAndCategoryIgnoreCase("Cervejas"))
                .thenReturn(produtos);

        String view = storeController.category("cervejas", model, null);

        assertEquals("client/category", view);

        verify(model).addAttribute("categoryName", "Cervejas");

        verify(model).addAttribute("products", produtos);

        verify(productRepository).findByActiveTrueAndCategoryIgnoreCase("Cervejas");
    }

    @Test
    void deveRejeitarCategoriaInexistente() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> storeController.category(
                        "categoria-inexistente",
                        model, null
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(model);
    }

    @Test
    void deveRetornarListaVaziaParaCategoriaSemProdutos() {

        when(productRepository
                .findByActiveTrueAndCategoryIgnoreCase("Gins"))
                .thenReturn(List.of());

        String view = storeController.category("gins", model, null);

        assertEquals("client/category", view);

        verify(model).addAttribute(
                "categoryName",
                "Gins"
        );

        verify(model).addAttribute(
                "products",
                List.of()
        );

        verify(productRepository)
                .findByActiveTrueAndCategoryIgnoreCase("Gins");
    }

    @Test
    void deveCarregarHomepageComProdutosAtivos() {

        Product produto = new Product();
        produto.setName("Cerveja Heineken");
        produto.setActive(true);

        List<Product> produtos = List.of(produto);

        when(productRepository.findByActiveTrue())
                .thenReturn(produtos);

        String view = storeController.home(model, null);

        assertEquals("client/homepage", view);

        verify(model).addAttribute(
                "products",
                produtos
        );

        verify(productRepository).findByActiveTrue();

        verify(productRepository, never())
                .findByActiveTrueAndCategoryIgnoreCase(anyString());
    }

    @Test
    void deveCarregarQuantidadesDoCarrinhoParaClienteAutenticado() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
                );

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(10L);
        item1.setQuantity(2);

        CartItemDTO item2 = new CartItemDTO();
        item2.setProductId(15L);
        item2.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item1, item2));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        Model model = mock(Model.class);

        storeController.category("cervejas", model, authentication);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of(10L, 2, 15L, 3))
        );

        verify(cartService).getCart("cliente@email.com");
    }

    @Test
    void deveRetornarQuantidadesVaziasQuandoCarrinhoEstiverVazio() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
                );

        CartDTO cart = new CartDTO();
        cart.setItems(List.of());

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        Model model = mock(Model.class);

        storeController.category("cervejas", model, authentication);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );
    }

    @Test
    void naoDeveConsultarCarrinhoParaVisitante() {

        Model model = mock(Model.class);

        storeController.category("cervejas", model, null);

        verifyNoInteractions(cartService);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );
    }

    @Test
    void naoDeveConsultarCarrinhoParaAdministrador() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@email.com",
                        "senha",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        Model model = mock(Model.class);

        storeController.category("cervejas", model, authentication);

        verifyNoInteractions(cartService);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );
    }


    @Test
    void deveCarregarQuantidadesDoCarrinhoNaHomepage() {


        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
                );


        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Cerveja Heineken");
        produto.setActive(true);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(produto));

        CartItemDTO item = new CartItemDTO();
        item.setProductId(1L);
        item.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String view = storeController.home(model, authentication);

        assertEquals("client/homepage", view);

        verify(model).addAttribute(
                "products",
                List.of(produto)
        );

        verify(model).addAttribute(
                "quantitiesByProductId",
                Map.of(1L, 3)
        );

        verify(cartService).getCart("cliente@email.com");
    }



}
