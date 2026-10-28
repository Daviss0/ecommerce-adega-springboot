package com.adega.adega.repository;

import com.adega.adega.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void deveBuscarProdutosPelaCategoriaExata() {

        Product cerveja = new Product();

        cerveja.setName("Cerveja teste");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);
        cerveja.setPrice(new java.math.BigDecimal("10.00"));
        cerveja.setStock(5);

        Product vinho = new Product();
        vinho.setName("Vinho teste");
        vinho.setCategory("Vinhos");
        vinho.setActive(true);
        vinho.setPrice(new java.math.BigDecimal("30.00"));
        vinho.setStock(5);

        productRepository.save(cerveja);
        productRepository.save(vinho);

        List<Product> resultado = productRepository.findByActiveTrueAndCategoryIgnoreCase("Cervejas");

        assertEquals(1, resultado.size());
        assertEquals("Cerveja teste", resultado.get(0).getName());
    }

    @Test
    void naoDeveRetornarProdutosInativos() {

        Product cervejaAtiva = new Product();
        cervejaAtiva.setName("Cerveja Ativa");
        cervejaAtiva.setCategory("Cervejas");
        cervejaAtiva.setActive(true);
        cervejaAtiva.setPrice(new BigDecimal("10.00"));
        cervejaAtiva.setStock(5);

        Product cervejaInativa = new Product();
        cervejaInativa.setName("Cerveja Inativa");
        cervejaInativa.setCategory("Cervejas");
        cervejaInativa.setActive(false);
        cervejaInativa.setPrice(new BigDecimal("12.00"));
        cervejaInativa.setStock(5);

        productRepository.save(cervejaAtiva);
        productRepository.save(cervejaInativa);

        List<Product> resultado =
                productRepository.findByActiveTrueAndCategoryIgnoreCase("Cervejas");

        assertEquals(1, resultado.size());
        assertEquals("Cerveja Ativa", resultado.get(0).getName());
    }

    @Test
    void deveIgnorarMaiusculasEMinusculasNaCategoria() {

        Product cerveja = new Product();
        cerveja.setName("Cerveja Teste");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);
        cerveja.setPrice(new BigDecimal("10.00"));
        cerveja.setStock(5);

        productRepository.save(cerveja);

        List<Product> resultado =
                productRepository.findByActiveTrueAndCategoryIgnoreCase("cervejas");

        assertEquals(1, resultado.size());
        assertEquals("Cerveja Teste", resultado.get(0).getName());
    }

    @Test
    void deveRetornarListaVaziaParaCategoriaInexistente() {

        Product cerveja = new Product();
        cerveja.setName("Cerveja Teste");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);
        cerveja.setPrice(new BigDecimal("10.00"));
        cerveja.setStock(5);

        productRepository.save(cerveja);

        List<Product> resultado =
                productRepository.findByActiveTrueAndCategoryIgnoreCase("Categoria Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void naoDeveRetornarCategoriasParciais() {

        Product cerveja = new Product();
        cerveja.setName("Cerveja Teste");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);
        cerveja.setPrice(new BigDecimal("10.00"));
        cerveja.setStock(5);

        Product cervejaArtesanal = new Product();
        cervejaArtesanal.setName("Cerveja Artesanal Teste");
        cervejaArtesanal.setCategory("Cervejas Artesanais");
        cervejaArtesanal.setActive(true);
        cervejaArtesanal.setPrice(new BigDecimal("20.00"));
        cervejaArtesanal.setStock(5);

        productRepository.save(cerveja);
        productRepository.save(cervejaArtesanal);

        List<Product> resultado =
                productRepository.findByActiveTrueAndCategoryIgnoreCase("Cervejas");

        assertEquals(1, resultado.size());
        assertEquals("Cerveja Teste", resultado.get(0).getName());
    }


    @Test
    void devePaginarTrezeProdutosEmDuasPaginas() {

        for (int i = 1; i <= 13; i++) {

            Product product = new Product();

            product.setName(String.format("Cerveja Teste %02d", i));
            product.setCategory("Cervejas");
            product.setActive(true);
            product.setPrice(new BigDecimal("10.00"));
            product.setStock(5);

            productRepository.save(product);
        }

        Sort sorting = Sort.by(Sort.Direction.ASC, "name");

        Pageable primeiraPagina = PageRequest.of(0, 12, sorting);
        Pageable segundaPagina = PageRequest.of(1, 12, sorting);

        Page<Product> resultadoPrimeiraPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0,
                                "Cervejas",
                                "",
                                primeiraPagina
                        );

        Page<Product> resultadoSegundaPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0,
                                "Cervejas",
                                "",
                                segundaPagina
                        );

        assertEquals(12, resultadoPrimeiraPagina.getContent().size());
        assertEquals(13, resultadoPrimeiraPagina.getTotalElements());
        assertEquals(2, resultadoPrimeiraPagina.getTotalPages());
        assertEquals(0, resultadoPrimeiraPagina.getNumber());

        assertEquals(1, resultadoSegundaPagina.getContent().size());
        assertEquals(13, resultadoSegundaPagina.getTotalElements());
        assertEquals(2, resultadoSegundaPagina.getTotalPages());
        assertEquals(1, resultadoSegundaPagina.getNumber());

        assertEquals(
                "Cerveja Teste 01",
                resultadoPrimeiraPagina.getContent().get(0).getName()
        );

        assertEquals(
                "Cerveja Teste 13",
                resultadoSegundaPagina.getContent().get(0).getName()
        );
    }

    @Test
    void deveExcluirProdutosInativosESemEstoqueDaPaginacao() {

        for (int i = 1; i <= 13; i++) {

            Product produto = new Product();
            produto.setName(String.format("Cerveja Teste %02d", i));
            produto.setCategory("Cervejas");
            produto.setActive(true);
            produto.setPrice(new BigDecimal("10.00"));
            produto.setStock(5);

            productRepository.save(produto);
        }

        Product inativo = new Product();
        inativo.setName("Cerveja Inativa");
        inativo.setCategory("Cervejas");
        inativo.setActive(false);
        inativo.setPrice(new BigDecimal("10.00"));
        inativo.setStock(5);

        productRepository.save(inativo);

        Product semEstoque = new Product();
        semEstoque.setName("Cerveja Sem Estoque");
        semEstoque.setCategory("Cervejas");
        semEstoque.setActive(true);
        semEstoque.setPrice(new BigDecimal("10.00"));
        semEstoque.setStock(0);

        productRepository.save(semEstoque);

        Pageable primeiraPagina = PageRequest.of(
                0, 12, Sort.by(Sort.Direction.ASC, "name")
        );

        Pageable segundaPagina = PageRequest.of(
                1, 12, Sort.by(Sort.Direction.ASC, "name")
        );

        Page<Product> resultadoPrimeiraPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", primeiraPagina
                        );

        Page<Product> resultadoSegundaPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", segundaPagina
                        );

        assertEquals(13, resultadoPrimeiraPagina.getTotalElements());
        assertEquals(2, resultadoPrimeiraPagina.getTotalPages());

        assertEquals(12, resultadoPrimeiraPagina.getContent().size());
        assertEquals(1, resultadoSegundaPagina.getContent().size());

        List<Product> todosProdutos = new ArrayList<>();
        todosProdutos.addAll(resultadoPrimeiraPagina.getContent());
        todosProdutos.addAll(resultadoSegundaPagina.getContent());

        assertTrue(todosProdutos.stream().allMatch(Product::getActive));

        assertTrue(todosProdutos.stream()
                .allMatch(produto -> produto.getStock() > 0));

        assertFalse(todosProdutos.stream()
                .anyMatch(produto -> produto.getName().equals("Cerveja Inativa")));

        assertFalse(todosProdutos.stream()
                .anyMatch(produto -> produto.getName().equals("Cerveja Sem Estoque")));
    }

    @Test
    void deveManterFiltroDePesquisaEntrePaginas() {

        for (int i = 1; i <= 13; i++) {

            Product produto = new Product();
            produto.setName(String.format("Heineken Teste %02d", i));
            produto.setCategory("Cervejas");
            produto.setActive(true);
            produto.setPrice(new BigDecimal("10.00"));
            produto.setStock(5);

            productRepository.save(produto);
        }

        for (String nome : List.of("Brahma", "Skol")) {

            Product produto = new Product();
            produto.setName(nome);
            produto.setCategory("Cervejas");
            produto.setActive(true);
            produto.setPrice(new BigDecimal("10.00"));
            produto.setStock(5);

            productRepository.save(produto);
        }

        Pageable primeiraPagina = PageRequest.of(
                0, 12, Sort.by(Sort.Direction.ASC, "name")
        );

        Pageable segundaPagina = PageRequest.of(
                1, 12, Sort.by(Sort.Direction.ASC, "name")
        );

        Page<Product> resultadoPrimeiraPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "Heineken", primeiraPagina
                        );

        Page<Product> resultadoSegundaPagina =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "Heineken", segundaPagina
                        );

        assertEquals(13, resultadoPrimeiraPagina.getTotalElements());
        assertEquals(2, resultadoPrimeiraPagina.getTotalPages());

        assertEquals(12, resultadoPrimeiraPagina.getContent().size());
        assertEquals(1, resultadoSegundaPagina.getContent().size());

        assertTrue(resultadoPrimeiraPagina.getContent().stream()
                .allMatch(produto -> produto.getName().contains("Heineken")));

        assertTrue(resultadoSegundaPagina.getContent().stream()
                .allMatch(produto -> produto.getName().contains("Heineken")));

        assertEquals(
                "Heineken Teste 01",
                resultadoPrimeiraPagina.getContent().get(0).getName()
        );

        assertEquals(
                "Heineken Teste 13",
                resultadoSegundaPagina.getContent().get(0).getName()
        );
    }

    @Test
    void deveOrdenarProdutosPorPrecoEntrePaginas() {

        for (int i = 1; i <= 13; i++) {

            Product produto = new Product();
            produto.setName(String.format("Cerveja Teste %02d", i));
            produto.setCategory("Cervejas");
            produto.setActive(true);
            produto.setPrice(BigDecimal.valueOf(i * 10L));
            produto.setStock(5);

            productRepository.save(produto);
        }

        Pageable crescentePagina0 = PageRequest.of(
                0, 12, Sort.by(Sort.Direction.ASC, "price")
        );

        Pageable crescentePagina1 = PageRequest.of(
                1, 12, Sort.by(Sort.Direction.ASC, "price")
        );

        Page<Product> resultadoCrescente0 =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", crescentePagina0
                        );

        Page<Product> resultadoCrescente1 =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", crescentePagina1
                        );

        assertEquals(13, resultadoCrescente0.getTotalElements());
        assertEquals(2, resultadoCrescente0.getTotalPages());
        assertEquals(12, resultadoCrescente0.getNumberOfElements());
        assertEquals(1, resultadoCrescente1.getNumberOfElements());

        assertEquals(
                0,
                resultadoCrescente0.getContent().get(0)
                        .getPrice().compareTo(new BigDecimal("10.00"))
        );

        assertEquals(
                0,
                resultadoCrescente1.getContent().get(0)
                        .getPrice().compareTo(new BigDecimal("130.00"))
        );

        Pageable decrescentePagina0 = PageRequest.of(
                0, 12, Sort.by(Sort.Direction.DESC, "price")
        );

        Pageable decrescentePagina1 = PageRequest.of(
                1, 12, Sort.by(Sort.Direction.DESC, "price")
        );

        Page<Product> resultadoDecrescente0 =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", decrescentePagina0
                        );

        Page<Product> resultadoDecrescente1 =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                                0, "Cervejas", "", decrescentePagina1
                        );

        assertEquals(13, resultadoDecrescente0.getTotalElements());
        assertEquals(2, resultadoDecrescente0.getTotalPages());
        assertEquals(12, resultadoDecrescente0.getNumberOfElements());
        assertEquals(1, resultadoDecrescente1.getNumberOfElements());

        assertEquals(
                0,
                resultadoDecrescente0.getContent().get(0)
                        .getPrice().compareTo(new BigDecimal("130.00"))
        );

        assertEquals(
                0,
                resultadoDecrescente1.getContent().get(0)
                        .getPrice().compareTo(new BigDecimal("10.00"))
        );
    }

}
