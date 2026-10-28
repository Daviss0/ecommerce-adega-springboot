package com.adega.adega.controller;


import com.adega.adega.controller.Client.StoreController;
import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.CartService;
import com.adega.adega.service.ProductService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
class StoreCategoryPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CartService cartService;

    @Test
    void devePreservarPesquisaEOrdenacaoNosLinksDePaginacao()
            throws Exception {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Heineken Long Neck");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);
        produto.setPrice(new BigDecimal("10.00"));

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(produto),
                PageRequest.of(1, 12),
                36
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq("Heineken"),
                eq("price_desc"),
                eq(1)
        )).thenReturn(paginaSimulada);

        String html = mockMvc.perform(
                        get("/store/category/cervejas")
                                .param("keyword", "Heineken")
                                .param("sort", "price_desc")
                                .param("page", "1")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Document document = Jsoup.parse(html);

        Element paginacao = document.selectFirst(
                "nav.category-pagination"
        );

        assertNotNull(paginacao);

        for (int pagina = 0; pagina < 3; pagina++) {

            String paginaEsperada = String.valueOf(pagina);

            boolean encontrouLink = paginacao
                    .select("a.page-link[href]")
                    .stream()
                    .anyMatch(link -> {
                        String href = link.attr("href");

                        return href.contains("keyword=Heineken")
                                && href.contains("sort=price_desc")
                                && href.contains("page=" + paginaEsperada);
                    });

            assertTrue(
                    encontrouLink,
                    "Não foi encontrado link para a página " + pagina
            );
        }
    }

    @Test
    void deveDesabilitarBotoesNasExtremidadesDaPaginacao()
            throws Exception {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Heineken Long Neck");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);
        produto.setPrice(new BigDecimal("10.00"));

        for (int paginaAtual : List.of(0, 2)) {

            Page<Product> paginaSimulada = new PageImpl<>(
                    List.of(produto),
                    PageRequest.of(paginaAtual, 12),
                    36
            );

            when(productService.findProductsByCategory(
                    eq("Cervejas"),
                    eq(""),
                    eq("name_asc"),
                    eq(paginaAtual)
            )).thenReturn(paginaSimulada);

            String html = mockMvc.perform(
                            get("/store/category/cervejas")
                                    .param("page", String.valueOf(paginaAtual))
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Document document = Jsoup.parse(html);

            Element paginacao = document.selectFirst(
                    "nav.category-pagination"
            );

            assertNotNull(paginacao);

            Element botaoAnterior = paginacao.selectFirst(
                    "a[aria-label='Página anterior']"
            );

            Element botaoProxima = paginacao.selectFirst(
                    "a[aria-label='Próxima página']"
            );

            assertNotNull(botaoAnterior);
            assertNotNull(botaoProxima);

            if (paginaAtual == 0) {

                assertTrue(
                        botaoAnterior.parent().hasClass("disabled")
                );

                assertFalse(
                        botaoProxima.parent().hasClass("disabled")
                );

            } else {

                assertFalse(
                        botaoAnterior.parent().hasClass("disabled")
                );

                assertTrue(
                        botaoProxima.parent().hasClass("disabled")
                );
            }
        }
    }

    @Test
    void naoDeveExibirPaginacaoQuandoExisteApenasUmaPagina()
            throws Exception {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Heineken Long Neck");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);
        produto.setPrice(new BigDecimal("10.00"));

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(produto),
                PageRequest.of(0, 12),
                1
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq(""),
                eq("name_asc"),
                eq(0)
        )).thenReturn(paginaSimulada);

        String html = mockMvc.perform(
                        get("/store/category/cervejas")
                                .param("page", "0")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Document document = Jsoup.parse(html);

        Element paginacao = document.selectFirst(
                "nav.category-pagination"
        );

        assertNull(
                paginacao,
                "A paginação não deveria aparecer quando existe apenas uma página"
        );

        assertTrue(
                document.text().contains("Heineken Long Neck")
        );
    }

    @Test
    void deveExibirMensagemQuandoCategoriaNaoPossuiProdutos()
            throws Exception {

        Page<Product> paginaVazia = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 12),
                0
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq(""),
                eq("name_asc"),
                eq(0)
        )).thenReturn(paginaVazia);

        String html = mockMvc.perform(
                        get("/store/category/cervejas")
                                .param("page", "0")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Document document = Jsoup.parse(html);

        Element mensagem = document.selectFirst(
                ".category-empty h3"
        );

        assertNotNull(mensagem);

        assertEquals(
                "Nenhum produto disponível",
                mensagem.text()
        );

        Element descricao = document.selectFirst(
                ".category-empty p"
        );

        assertNotNull(descricao);

        assertEquals(
                "Ainda não temos produtos disponíveis nesta categoria.",
                descricao.text()
        );

        Element botaoVoltar = document.selectFirst(
                ".category-empty a"
        );

        assertNotNull(botaoVoltar);

        assertEquals(
                "Voltar para a loja",
                botaoVoltar.text()
        );

        assertEquals(
                "/store/homepage",
                botaoVoltar.attr("href")
        );

        assertNull(
                document.selectFirst("nav.category-pagination"),
                "A paginação não deveria aparecer em uma categoria vazia"
        );

        assertTrue(
                document.select(".product-card").isEmpty(),
                "Não deveria existir nenhum card de produto"
        );
    }
}