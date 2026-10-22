package com.adega.adega.controller;

import com.adega.adega.controller.Client.CartController;
import com.adega.adega.service.CartService;
import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

public class CartControllerTest {

    private CartService cartService;
    private CartController cartController;
    private Authentication authentication;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        cartController = new CartController(cartService);

        authentication = mock(Authentication.class);
        redirectAttributes = mock(RedirectAttributes.class);

        when(authentication.getName()).thenReturn("cliente@email.com");
    }

    @Test
    void deveAdicionarUmaUnidadePelaVitrine() {

        String redirect = cartController.increaseProductFromStore(
                10L,
                null,
                authentication,
                redirectAttributes
        );

        verify(cartService).addProduct(
                eq("cliente@email.com"),
                argThat(dto ->
                        dto.getProductId().equals(10L)
                                && dto.getQuantity().equals(1)
                )
        );

        assertEquals("redirect:/store/homepage", redirect);
    }

    @Test
    void deveDiminuirUmaUnidadePelaVitrine() {

        CartItemDTO item = new CartItemDTO();
        item.setId(5L);
        item.setProductId(10L);
        item.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String redirect = cartController.decreaseProductFromStore(
                10L,
                null,
                authentication,
                redirectAttributes
        );

        verify(cartService).decreaseQuantity(
                "cliente@email.com",
                5L
        );

        verify(cartService, never()).removeItem(any(), any());

        assertEquals("redirect:/store/homepage", redirect);
    }

    @Test
    void deveRemoverProdutoQuandoQuantidadeForUm() {

        CartItemDTO item = new CartItemDTO();
        item.setId(5L);
        item.setProductId(10L);
        item.setQuantity(1);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String redirect = cartController.decreaseProductFromStore(
                10L,
                null,
                authentication,
                redirectAttributes
        );

        verify(cartService).removeItem(
                "cliente@email.com",
                5L
        );

        verify(cartService, never()).decreaseQuantity(any(), any());

        assertEquals("redirect:/store/homepage", redirect);
    }


    @Test
    void naoDeveDiminuirProdutoInexistenteNoCarrinho() {

        CartDTO cart = new CartDTO();
        cart.setItems(List.of());

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String redirect = cartController.decreaseProductFromStore(
                10L,
                null,
                authentication,
                redirectAttributes
        );

        verify(cartService, never()).decreaseQuantity(any(), any());
        verify(cartService, never()).removeItem(any(), any());

        assertEquals("redirect:/store/homepage", redirect);
    }


    @Test
    void deveUtilizarClienteAutenticadoParaAlterarCarrinho() {

        Authentication outroCliente = mock(Authentication.class);

        when(outroCliente.getName())
                .thenReturn("outrocliente@email.com");

        cartController.increaseProductFromStore(
                10L,
                null,
                outroCliente,
                redirectAttributes
        );

        verify(cartService).addProduct(
                eq("outrocliente@email.com"),
                argThat(dto ->
                        dto.getProductId().equals(10L)
                                && dto.getQuantity().equals(1)
                )
        );

        verify(cartService, never()).addProduct(
                eq("cliente@email.com"),
                any(AddCartDTO.class)
        );
    }

    @Test
    void devePermanecerNaCategoriaAposAumentarQuantidade() {

        String redirect = cartController.increaseProductFromStore(
                10L,
                "cervejas",
                authentication,
                redirectAttributes
        );

        assertEquals("redirect:/store/category/cervejas", redirect);

        verify(cartService).addProduct(
                eq("cliente@email.com"),
                any(AddCartDTO.class)
        );
    }

    @Test
    void devePermanecerNaCategoriaAposDiminuirQuantidade() {

        CartItemDTO item = new CartItemDTO();
        item.setId(5L);
        item.setProductId(10L);
        item.setQuantity(2);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String redirect = cartController.decreaseProductFromStore(
                10L,
                "cervejas",
                authentication,
                redirectAttributes
        );

        assertEquals("redirect:/store/category/cervejas", redirect);

        verify(cartService).decreaseQuantity("cliente@email.com", 5L);
    }

    @Test
    void deveRejeitarCategoriaInvalidaNoRedirecionamento() {

        String redirect = cartController.increaseProductFromStore(
                10L,
                "categoria-inexistente",
                authentication,
                redirectAttributes
        );

        assertEquals("redirect:/store/homepage", redirect);
    }


    @Test
    void deveAumentarQuantidadeViaAjaxERetornarCarrinhoAtualizado() {

        CartItemDTO item = new CartItemDTO();
        item.setId(5L);
        item.setProductId(10L);
        item.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        when(cartService.getTotalItems("cliente@email.com"))
                .thenReturn(3);

        when(cartService.getCartTotal("cliente@email.com"))
                .thenReturn(new BigDecimal("45.00"));

        var response = cartController.increaseProductAjax(
                10L,
                authentication
        );

        assertEquals(200, response.getStatusCode().value());

        var body = response.getBody();

        assertEquals(10L, body.get("productId"));
        assertEquals(3, body.get("quantity"));
        assertEquals(3, body.get("cartItemCount"));
        assertEquals(new BigDecimal("45.00"), body.get("cartTotal"));

        verify(cartService).addProduct(
                eq("cliente@email.com"),
                argThat(dto ->
                        dto.getProductId().equals(10L)
                                && dto.getQuantity().equals(1)
                )
        );

        verify(cartService).getCart("cliente@email.com");
        verify(cartService).getTotalItems("cliente@email.com");
        verify(cartService).getCartTotal("cliente@email.com");
    }


    @Test
    void deveDiminuirQuantidadeViaAjaxERetornarCarrinhoAtualizado() {

        CartItemDTO itemAntes = new CartItemDTO();
        itemAntes.setId(5L);
        itemAntes.setProductId(10L);
        itemAntes.setQuantity(3);

        CartDTO cartAntes = new CartDTO();
        cartAntes.setItems(List.of(itemAntes));

        CartItemDTO itemDepois = new CartItemDTO();
        itemDepois.setId(5L);
        itemDepois.setProductId(10L);
        itemDepois.setQuantity(2);

        CartDTO cartDepois = new CartDTO();
        cartDepois.setItems(List.of(itemDepois));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cartAntes, cartDepois);

        when(cartService.getTotalItems("cliente@email.com"))
                .thenReturn(2);

        when(cartService.getCartTotal("cliente@email.com"))
                .thenReturn(new BigDecimal("30.00"));

        var response = cartController.decreaseProductAjax(
                10L,
                authentication
        );

        assertEquals(200, response.getStatusCode().value());

        var body = response.getBody();

        assertEquals(10L, body.get("productId"));
        assertEquals(2, body.get("quantity"));
        assertEquals(2, body.get("cartItemCount"));
        assertEquals(new BigDecimal("30.00"), body.get("cartTotal"));

        verify(cartService).decreaseQuantity(
                "cliente@email.com",
                5L
        );

        verify(cartService, never()).removeItem(any(), any());

        verify(cartService, times(2)).getCart("cliente@email.com");

        verify(cartService).getTotalItems("cliente@email.com");
        verify(cartService).getCartTotal("cliente@email.com");
    }


    @Test
    void deveRemoverProdutoViaAjaxQuandoQuantidadeForUm() {

        CartItemDTO item = new CartItemDTO();
        item.setId(5L);
        item.setProductId(10L);
        item.setQuantity(1);

        CartDTO cartAntes = new CartDTO();
        cartAntes.setItems(List.of(item));

        CartDTO cartDepois = new CartDTO();
        cartDepois.setItems(List.of());

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cartAntes, cartDepois);

        when(cartService.getTotalItems("cliente@email.com"))
                .thenReturn(0);

        when(cartService.getCartTotal("cliente@email.com"))
                .thenReturn(BigDecimal.ZERO);

        var response = cartController.decreaseProductAjax(
                10L,
                authentication
        );

        assertEquals(200, response.getStatusCode().value());

        var body = response.getBody();

        assertEquals(10L, body.get("productId"));
        assertEquals(0, body.get("quantity"));
        assertEquals(0, body.get("cartItemCount"));
        assertEquals(BigDecimal.ZERO, body.get("cartTotal"));

        verify(cartService).removeItem(
                "cliente@email.com",
                5L
        );

        verify(cartService, never()).decreaseQuantity(any(), any());

        verify(cartService, times(2)).getCart("cliente@email.com");

        verify(cartService).getTotalItems("cliente@email.com");
        verify(cartService).getCartTotal("cliente@email.com");
    }


    @Test
    void naoDeveDiminuirProdutoInexistenteViaAjax() {

        CartDTO cart = new CartDTO();
        cart.setItems(List.of());

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        when(cartService.getTotalItems("cliente@email.com"))
                .thenReturn(0);

        when(cartService.getCartTotal("cliente@email.com"))
                .thenReturn(BigDecimal.ZERO);

        var response = cartController.decreaseProductAjax(
                10L,
                authentication
        );

        assertEquals(200, response.getStatusCode().value());

        var body = response.getBody();

        assertEquals(10L, body.get("productId"));
        assertEquals(0, body.get("quantity"));
        assertEquals(0, body.get("cartItemCount"));
        assertEquals(BigDecimal.ZERO, body.get("cartTotal"));

        verify(cartService, never()).decreaseQuantity(any(), any());
        verify(cartService, never()).removeItem(any(), any());
        verify(cartService, never()).addProduct(any(), any());

        verify(cartService, times(2)).getCart("cliente@email.com");

        verify(cartService).getTotalItems("cliente@email.com");
        verify(cartService).getCartTotal("cliente@email.com");
    }



}
