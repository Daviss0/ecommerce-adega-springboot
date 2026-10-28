package com.adega.adega.service.impl;

import com.adega.adega.entity.Product;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.repository.OrderItemRepository;
import com.adega.adega.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void deveRetornarProdutosEmOrdemAlfabeticaQuandoNaoHouverVendas() {

        Product whisky = criarProduto(1L, "Whisky", 10);
        Product cerveja = criarProduto(2L, "Cerveja", 20);
        Product vodka = criarProduto(3L, "Vodka", 15);

        when(orderItemRepository.findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(whisky, cerveja, vodka));

        List<Product> resultado =
                productService.findHomepageFeaturedProducts();

        assertEquals(3, resultado.size());

        assertEquals("Cerveja", resultado.get(0).getName());
        assertEquals("Vodka", resultado.get(1).getName());
        assertEquals("Whisky", resultado.get(2).getName());

        verify(orderItemRepository).findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        );

        verify(productRepository).findByActiveTrue();
    }

    private Product criarProduto(Long id, String nome, int estoque) {

        Product product = new Product();

        product.setId(id);
        product.setName(nome);
        product.setStock(estoque);
        product.setActive(true);

        return product;
    }


    @Test
    void devePriorizarMaisVendidosECompletarVagasComOutrosProdutos() {

        Product whisky = criarProduto(1L, "Whisky", 10);
        Product vodka = criarProduto(2L, "Vodka", 15);
        Product cerveja = criarProduto(3L, "Cerveja", 20);
        Product agua = criarProduto(4L, "Água", 30);
        Product refrigerante = criarProduto(5L, "Refrigerante", 25);

        when(orderItemRepository.findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        )).thenReturn(List.<Object[]>of(
                new Object[]{1L, 20L},
                new Object[]{2L, 10L}
        ));

        when(productRepository.findById(1L))
                .thenReturn(java.util.Optional.of(whisky));

        when(productRepository.findById(2L))
                .thenReturn(java.util.Optional.of(vodka));


        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(
                        refrigerante, whisky, cerveja, vodka, agua
                ));

        List<Product> resultado =
                productService.findHomepageFeaturedProducts();

        assertEquals(5, resultado.size());

        assertEquals("Whisky", resultado.get(0).getName());
        assertEquals("Vodka", resultado.get(1).getName());
        assertEquals("Água", resultado.get(2).getName());
        assertEquals("Cerveja", resultado.get(3).getName());
        assertEquals("Refrigerante", resultado.get(4).getName());

        assertEquals(
                5,
                resultado.stream()
                        .map(Product::getId)
                        .distinct()
                        .count()
        );

        verify(productRepository).findByActiveTrue();
    }


    @Test
    void deveRetornarSomenteOitoProdutosQuandoHouverMuitasVendas() {

        List<Object[]> ranking = new ArrayList<>();

        for (long id = 1; id <= 12; id++) {

            long quantidadeVendida = 130 - (id * 10);

            ranking.add(new Object[]{
                    id,
                    quantidadeVendida
            });

            Product product = criarProduto(
                    id,
                    "Produto " + id,
                    20
            );

            if (id <= 8) {
                when(productRepository.findById(id))
                        .thenReturn(java.util.Optional.of(product));
            }
        }

        when(orderItemRepository.findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        )).thenReturn(ranking);

        List<Product> resultado =
                productService.findHomepageFeaturedProducts();

        assertEquals(8, resultado.size());

        for (int i = 0; i < 8; i++) {
            assertEquals(
                    (long) i + 1,
                    resultado.get(i).getId()
            );
        }

        verify(productRepository, never())
                .findByActiveTrue();

        verify(productRepository, never())
                .findById(9L);
    }


    @Test
    void deveIgnorarProdutosInativosOuSemEstoque() {

        Product whisky = criarProduto(1L, "Whisky", 10);

        Product vodka = criarProduto(2L, "Vodka", 20);
        vodka.setActive(false);

        Product cerveja = criarProduto(3L, "Cerveja", 0);

        Product agua = criarProduto(4L, "Água", 15);
        Product refrigerante = criarProduto(5L, "Refrigerante", 8);

        when(orderItemRepository.findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        )).thenReturn(List.<Object[]>of(
                new Object[]{2L, 50L},
                new Object[]{3L, 40L},
                new Object[]{1L, 30L}
        ));

        when(productRepository.findById(2L))
                .thenReturn(java.util.Optional.of(vodka));

        when(productRepository.findById(3L))
                .thenReturn(java.util.Optional.of(cerveja));

        when(productRepository.findById(1L))
                .thenReturn(java.util.Optional.of(whisky));

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(
                        whisky, cerveja, agua, refrigerante
                ));

        List<Product> resultado =
                productService.findHomepageFeaturedProducts();

        assertEquals(3, resultado.size());

        assertEquals("Whisky", resultado.get(0).getName());

        assertEquals("Água", resultado.get(1).getName());
        assertEquals("Refrigerante", resultado.get(2).getName());

        assertTrue(resultado.stream().allMatch(product ->
                Boolean.TRUE.equals(product.getActive())
                        && product.getStock() != null
                        && product.getStock() > 0
        ));

        assertFalse(resultado.contains(vodka));
        assertFalse(resultado.contains(cerveja));
    }


    @Test
    void naoDeveAdicionarProdutosDuplicadosNaVitrine() {

        Product whisky = criarProduto(1L, "Whisky", 10);
        Product vodka = criarProduto(2L, "Vodka", 15);
        Product cerveja = criarProduto(3L, "Cerveja", 20);
        Product agua = criarProduto(4L, "Água", 25);

        when(orderItemRepository.findBestSellingProducts(
                eq(OrderStatus.DELIVERED),
                any(LocalDateTime.class)
        )).thenReturn(List.<Object[]>of(
                new Object[]{1L, 30L},
                new Object[]{1L, 20L},
                new Object[]{2L, 10L}
        ));

        when(productRepository.findById(1L))
                .thenReturn(java.util.Optional.of(whisky));

        when(productRepository.findById(2L))
                .thenReturn(java.util.Optional.of(vodka));

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(
                        cerveja, whisky, agua, vodka
                ));

        List<Product> resultado =
                productService.findHomepageFeaturedProducts();

        assertEquals(4, resultado.size());

        assertEquals("Whisky", resultado.get(0).getName());
        assertEquals("Vodka", resultado.get(1).getName());
        assertEquals("Água", resultado.get(2).getName());
        assertEquals("Cerveja", resultado.get(3).getName());

        long quantidadeIdsDistintos = resultado.stream()
                .map(Product::getId)
                .distinct()
                .count();

        assertEquals(resultado.size(), quantidadeIdsDistintos);

        verify(productRepository, times(2)).findById(1L);
    }


    @Test
    void deveUtilizarPaginaZeroComDozeProdutosEOrdenacaoPadrao() {

        Page<Product> paginaSimulada = new PageImpl<>(List.of());

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        any(Pageable.class)
                ))
                .thenReturn(paginaSimulada);

        Page<Product> resultado = productService.findProductsByCategory(
                "Cervejas",
                "",
                "name_asc",
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository)
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(12, pageable.getPageSize());

        Sort.Order ordenacao = pageable.getSort().getOrderFor("name");

        assertNotNull(ordenacao);
        assertEquals(Sort.Direction.ASC, ordenacao.getDirection());

        assertSame(paginaSimulada, resultado);
    }

    @Test
    void deveConverterPaginaNegativaParaZero() {

        Page<Product> paginaSimulada = new PageImpl<>(List.of());

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        any(Pageable.class)
                ))
                .thenReturn(paginaSimulada);

        Page<Product> resultado = productService.findProductsByCategory(
                "Cervejas",
                "",
                "name_asc",
                -5
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository)
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());

        assertEquals(12, pageable.getPageSize());

        assertSame(paginaSimulada, resultado);
    }

    @Test
    void deveOrdenarProdutosPorNomeCrescenteEDecrescente() {

        Page<Product> paginaSimulada = new PageImpl<>(List.of());

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        any(Pageable.class)
                ))
                .thenReturn(paginaSimulada);

        productService.findProductsByCategory(
                "Cervejas",
                "",
                "name_asc",
                0
        );

        productService.findProductsByCategory(
                "Cervejas",
                "",
                "name_desc",
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository, times(2))
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        pageableCaptor.capture()
                );

        Pageable pageableCrescente = pageableCaptor.getAllValues().get(0);
        Pageable pageableDecrescente = pageableCaptor.getAllValues().get(1);

        Sort.Order ordemCrescente =
                pageableCrescente.getSort().getOrderFor("name");

        assertNotNull(ordemCrescente);
        assertEquals(
                Sort.Direction.ASC,
                ordemCrescente.getDirection()
        );

        Sort.Order ordemDecrescente =
                pageableDecrescente.getSort().getOrderFor("name");

        assertNotNull(ordemDecrescente);
        assertEquals(
                Sort.Direction.DESC,
                ordemDecrescente.getDirection()
        );

        assertEquals(12, pageableCrescente.getPageSize());
        assertEquals(12, pageableDecrescente.getPageSize());
    }

    @Test
    void deveOrdenarProdutosPorPrecoCrescenteEDecrescente() {

        Page<Product> paginaSimulada = new PageImpl<>(List.of());

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        any(Pageable.class)
                ))
                .thenReturn(paginaSimulada);

        productService.findProductsByCategory(
                "Cervejas",
                "",
                "price_asc",
                0
        );

        productService.findProductsByCategory(
                "Cervejas",
                "",
                "price_desc",
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository, times(2))
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        pageableCaptor.capture()
                );

        Pageable pageableCrescente = pageableCaptor.getAllValues().get(0);
        Pageable pageableDecrescente = pageableCaptor.getAllValues().get(1);

        Sort.Order ordemCrescente =
                pageableCrescente.getSort().getOrderFor("price");

        assertNotNull(ordemCrescente);
        assertEquals(
                Sort.Direction.ASC,
                ordemCrescente.getDirection()
        );

        Sort.Order ordemDecrescente =
                pageableDecrescente.getSort().getOrderFor("price");

        assertNotNull(ordemDecrescente);
        assertEquals(
                Sort.Direction.DESC,
                ordemDecrescente.getDirection()
        );

        assertEquals(0, pageableCrescente.getPageNumber());
        assertEquals(0, pageableDecrescente.getPageNumber());

        assertEquals(12, pageableCrescente.getPageSize());
        assertEquals(12, pageableDecrescente.getPageSize());


        assertEquals(
                "price",
                pageableCrescente.getSort().iterator().next().getProperty()
        );

        assertEquals(
                "price",
                pageableDecrescente.getSort().iterator().next().getProperty()
        );
    }

    @Test
    void deveUtilizarOrdenacaoPadraoQuandoSortForInvalido() {

        Page<Product> paginaSimulada = new PageImpl<>(List.of());

        when(productRepository
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        any(Pageable.class)
                ))
                .thenReturn(paginaSimulada);

        Page<Product> resultado = productService.findProductsByCategory(
                "Cervejas",
                "",
                "qualquer_coisa",
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository)
                .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                        eq(0),
                        eq("Cervejas"),
                        eq(""),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        Sort.Order ordenacao =
                pageable.getSort().getOrderFor("name");

        assertNotNull(ordenacao);

        assertEquals(
                Sort.Direction.ASC,
                ordenacao.getDirection()
        );

        assertEquals(
                "name",
                pageable.getSort().iterator().next().getProperty()
        );

        assertEquals(0, pageable.getPageNumber());
        assertEquals(12, pageable.getPageSize());

        assertSame(paginaSimulada, resultado);
    }

}