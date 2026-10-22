package com.adega.adega.repository;

import com.adega.adega.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
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
}
