package com.adega.adega.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ProductCategoriesTest {

    @Test
    void deveConverterCategoriaParaSlug() {
        assertEquals(
                "bebidas-sem-alcool",
                ProductCategories.toSlug("Bebidas sem álcool")
        );

        assertEquals(
                "acessorios-para-narguile",
                ProductCategories.toSlug("Acessórios para narguilé")
        );
    }

    @Test
    void deveEncontrarCategoriaPeloSlug() {
        assertEquals("Cervejas", ProductCategories.fromSlug("cervejas").orElseThrow());
    }

    @Test
    void deveRejeitarSlugInexistente() {
        assertTrue(ProductCategories.fromSlug("categoria-falsa").isEmpty());
    }
}
