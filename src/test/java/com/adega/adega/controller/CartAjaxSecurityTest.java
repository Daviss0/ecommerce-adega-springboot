package com.adega.adega.controller;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.adega.adega.service.CartService;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartAjaxSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void naoDevePermitirAumentoAjaxSemAutenticacao() throws Exception {

        mockMvc.perform(
                        post("/client/cart/ajax/products/10/increase")
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void deveRedirecionarClienteNaoAutenticadoMesmoComCsrfValido() throws Exception {

        mockMvc.perform(
                        post("/client/cart/ajax/products/10/increase")
                                .with(csrf())
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/login"));
    }


    @Test
    void deveBloquearAumentoAjaxSemCsrfMesmoComClienteAutenticado() throws Exception {

        mockMvc.perform(
                        post("/client/cart/ajax/products/10/increase")
                                .with(user("cliente@email.com").roles("CLIENT"))
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void naoDevePermitirAdminAumentarQuantidadeViaAjax() throws Exception {

        mockMvc.perform(
                        post("/client/cart/ajax/products/10/increase")
                                .with(user("admin@email.com").roles("ADMIN"))
                                .with(csrf())
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void devePermitirAumentoAjaxComClienteAutenticadoECsrfValido() throws Exception {

        String email = "cliente@email.com";
        Long productId = 10L;

        CartItemDTO item = mock(CartItemDTO.class);
        when(item.getProductId()).thenReturn(productId);
        when(item.getQuantity()).thenReturn(3);

        CartDTO cart = mock(CartDTO.class);
        when(cart.getItems()).thenReturn(List.of(item));

        when(cartService.getCart(email)).thenReturn(cart);
        when(cartService.getTotalItems(email)).thenReturn(3);
        when(cartService.getCartTotal(email))
                .thenReturn(new BigDecimal("45.00"));

        mockMvc.perform(
                        post("/client/cart/ajax/products/{productId}/increase", productId)
                                .with(user(email).roles("CLIENT"))
                                .with(csrf())
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.cartItemCount").value(3))
                .andExpect(jsonPath("$.cartTotal").value(45.00));

        verify(cartService).addProduct(
                eq(email),
                argThat(dto ->
                        productId.equals(dto.getProductId())
                                && dto.getQuantity() == 1
                )
        );
    }


    @Test
    void naoDeveAlterarCarrinhoQuandoAdminTentaAumentarQuantidadeViaAjax() throws Exception {

        mockMvc.perform(
                        post("/client/cart/ajax/products/10/increase")
                                .with(user("admin@email.com").roles("ADMIN"))
                                .with(csrf())
                                .header("Accept", "application/json")
                                .header("X-Requested-With", "XMLHttpRequest")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(cartService);
    }



}