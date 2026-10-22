package com.adega.adega.repository;

import com.adega.adega.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositorySearchTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void deveEncontrarProdutoPesquisandoParteDoNome() {

        Product product = new Product();
        product.setName("Heineken Long Neck 330ml");
        product.setCategory("Cerveja");
        product.setPrice(new BigDecimal("7.99"));
        product.setStock(20);
        product.setActive(true);

        productRepository.saveAndFlush(product);

        List<Product> results =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "Heineken"
                        );

        assertEquals(1, results.size());
        assertEquals("Heineken Long Neck 330ml", results.get(0).getName());
    }


    @Test
    void deveIgnorarMaiusculasEMinusculasNaPesquisa() {

        Product product = new Product();
        product.setName("Heineken Long Neck 330ml");
        product.setCategory("Cerveja");
        product.setPrice(new BigDecimal("7.99"));
        product.setStock(20);
        product.setActive(true);

        productRepository.saveAndFlush(product);

        List<Product> resultsUpper =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "HEINEKEN"
                        );

        List<Product> resultsLower =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "heineken"
                        );

        List<Product> resultsMixed =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "HeInEkEn"
                        );

        assertEquals(1, resultsUpper.size());
        assertEquals(1, resultsLower.size());
        assertEquals(1, resultsMixed.size());

        assertEquals(product.getId(), resultsUpper.get(0).getId());
        assertEquals(product.getId(), resultsLower.get(0).getId());
        assertEquals(product.getId(), resultsMixed.get(0).getId());
    }


    @Test
    void naoDeveRetornarProdutosInativosNaPesquisa() {

        Product activeProduct = new Product();
        activeProduct.setName("Heineken Long Neck 330ml");
        activeProduct.setCategory("Cerveja");
        activeProduct.setPrice(new BigDecimal("7.99"));
        activeProduct.setStock(20);
        activeProduct.setActive(true);

        Product inactiveProduct = new Product();
        inactiveProduct.setName("Heineken Lata 350ml");
        inactiveProduct.setCategory("Cerveja");
        inactiveProduct.setPrice(new BigDecimal("5.99"));
        inactiveProduct.setStock(15);
        inactiveProduct.setActive(false);

        productRepository.saveAndFlush(activeProduct);
        productRepository.saveAndFlush(inactiveProduct);

        List<Product> results =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "Heineken"
                        );

        assertEquals(1, results.size());
        assertEquals(activeProduct.getId(), results.get(0).getId());
        assertTrue(results.get(0).getActive());
    }


    @Test
    void naoDeveRetornarProdutosSemEstoqueNaPesquisa() {

        Product availableProduct = new Product();
        availableProduct.setName("Heineken Long Neck 330ml");
        availableProduct.setCategory("Cerveja");
        availableProduct.setPrice(new BigDecimal("7.99"));
        availableProduct.setStock(20);
        availableProduct.setActive(true);

        Product outOfStockProduct = new Product();
        outOfStockProduct.setName("Heineken Lata 350ml");
        outOfStockProduct.setCategory("Cerveja");
        outOfStockProduct.setPrice(new BigDecimal("5.99"));
        outOfStockProduct.setStock(0);
        outOfStockProduct.setActive(true);

        productRepository.saveAndFlush(availableProduct);
        productRepository.saveAndFlush(outOfStockProduct);

        List<Product> results =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "Heineken"
                        );

        assertEquals(1, results.size());
        assertEquals(availableProduct.getId(), results.get(0).getId());
        assertTrue(results.get(0).getStock() > 0);
    }


    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrarProdutos() {

        Product product = new Product();
        product.setName("Heineken Long Neck 330ml");
        product.setCategory("Cerveja");
        product.setPrice(new BigDecimal("7.99"));
        product.setStock(20);
        product.setActive(true);

        productRepository.saveAndFlush(product);

        List<Product> results =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "Produto Inexistente"
                        );

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }


    @Test
    void deveRetornarProdutosEmOrdemAlfabetica() {

        Product product1 = new Product();
        product1.setName("Heineken Long Neck 330ml");
        product1.setCategory("Cerveja");
        product1.setPrice(new BigDecimal("7.99"));
        product1.setStock(20);
        product1.setActive(true);

        Product product2 = new Product();
        product2.setName("Heineken Barril 5L");
        product2.setCategory("Cerveja");
        product2.setPrice(new BigDecimal("89.90"));
        product2.setStock(10);
        product2.setActive(true);

        Product product3 = new Product();
        product3.setName("Heineken Lata 350ml");
        product3.setCategory("Cerveja");
        product3.setPrice(new BigDecimal("5.99"));
        product3.setStock(30);
        product3.setActive(true);

        productRepository.saveAndFlush(product1);
        productRepository.saveAndFlush(product3);
        productRepository.saveAndFlush(product2);

        List<Product> results =
                productRepository
                        .findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
                                0, "Heineken"
                        );

        assertEquals(3, results.size());

        assertEquals("Heineken Barril 5L", results.get(0).getName());
        assertEquals("Heineken Lata 350ml", results.get(1).getName());
        assertEquals("Heineken Long Neck 330ml", results.get(2).getName());
    }





}