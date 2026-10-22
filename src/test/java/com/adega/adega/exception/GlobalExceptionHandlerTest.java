package com.adega.adega.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI())
                .thenReturn("/store/category/categoria-inexistente");
    }

    @Test
    void deveRetornarPagina404ParaCategoriaInexistente() {

        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                );

        String view = handler.handleResponseStatusException(
                exception,
                request,
                response
        );

        assertEquals("error/404", view);

        verify(response).setStatus(404);
    }

    @Test
    void devePreservarStatusDeOutraResponseStatusException() {

        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Requisição inválida"
                );

        String view = handler.handleResponseStatusException(
                exception,
                request,
                response
        );

        assertEquals("error/500", view);

        verify(response).setStatus(400);
    }

    @Test
    void deveRetornarPagina500ParaErroInesperado() {

        RuntimeException exception =
                new RuntimeException("Erro inesperado de teste");

        String view = handler.handleUnexpectedException(
                exception,
                request
        );

        assertEquals("error/500", view);
    }
}