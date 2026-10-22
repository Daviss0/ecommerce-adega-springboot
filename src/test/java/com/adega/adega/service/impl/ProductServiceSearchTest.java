package com.adega.adega.service.impl;

import com.adega.adega.entity.Product;
import com.adega.adega.repository.OrderItemRepository;
import com.adega.adega.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceSearchTest {

    private ProductRepository productRepository;
    private OrderItemRepository orderItemRepository;
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);

        productService = new ProductServiceImpl(
                productRepository,
                orderItemRepository
        );
    }

    @Test
    void devePesquisarProdutosPeloNome() {

        Product product = new Product();
        product.setName("Heineken Long Neck 330ml");
        product.setCategory("Cerveja");
        product.setPrice(new BigDecimal("7.99"));
        product.setStock(20);
        product.setActive(true);

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Heineken"
                ))
                .thenReturn(List.of(product));

        List<Product> results =
                productService.searchProducts("Heineken");

        assertEquals(1, results.size());
        assertEquals("Heineken Long Neck 330ml",
                results.get(0).getName());

        verify(productRepository, times(1))
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Heineken"
                );
    }


    @Test
    void deveRemoverEspacosAntesDePesquisar() {

        Product product = new Product();
        product.setName("Heineken Long Neck 330ml");
        product.setCategory("Cerveja");
        product.setPrice(new BigDecimal("7.99"));
        product.setStock(20);
        product.setActive(true);

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Heineken"
                ))
                .thenReturn(List.of(product));

        List<Product> results =
                productService.searchProducts("   Heineken   ");

        assertEquals(1, results.size());
        assertEquals("Heineken Long Neck 330ml",
                results.get(0).getName());

        verify(productRepository, times(1))
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Heineken"
                );
    }


    @Test
    void naoDeveConsultarRepositoryQuandoPesquisaForVazia() {

        List<Product> emptyResults =
                productService.searchProducts("");

        List<Product> blankResults =
                productService.searchProducts("   ");

        List<Product> nullResults =
                productService.searchProducts(null);

        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());

        assertNotNull(blankResults);
        assertTrue(blankResults.isEmpty());

        assertNotNull(nullResults);
        assertTrue(nullResults.isEmpty());

        verifyNoInteractions(productRepository);
    }


    @Test
    void deveRetornarListaVaziaQuandoNenhumProdutoForEncontrado() {

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Produto Inexistente"
                ))
                .thenReturn(List.of());

        List<Product> results =
                productService.searchProducts("Produto Inexistente");

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(productRepository, times(1))
                .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                        0, "Produto Inexistente"
                );
    }

}