
package com.adega.adega.controller;

import com.adega.adega.controller.Client.StoreController;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.CartService;
import com.adega.adega.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class StoreControllerTest {

    private ProductRepository productRepository;
    private StoreController storeController;
    private Model model;
    private CartService cartService;
    private ProductService productService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);
        model = mock(Model.class);
        cartService = mock(CartService.class);
        productService = mock(ProductService.class);

        storeController = new StoreController(
                productRepository,
                cartService,
                productService
        );

        when(productService.findProductsByCategory(
                anyString(),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(Page.empty());
    }

    @Test
    void deveCarregarCategoriaValida() {

        Product cerveja = new Product();
        cerveja.setName("Cerveja Heineken");
        cerveja.setCategory("Cervejas");
        cerveja.setActive(true);

        List<Product> produtos = List.of(cerveja);

        Page<Product> pagina = new PageImpl<>(produtos);

        when(productService.findProductsByCategory(
                "Cervejas", "", "name_asc", 0
        )).thenReturn(pagina);

        String view = storeController.category(
                "cervejas",
                null,
                "name_asc",
                0,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(model).addAttribute(
                "categoryName", "Cervejas"
        );

        verify(model).addAttribute(
                "products", produtos
        );

        verify(model).addAttribute(
                "currentPage", 0
        );

        verify(model).addAttribute(
                "totalPages", 1
        );

        verify(model).addAttribute(
                "totalProducts", 1L
        );

        verify(productService).findProductsByCategory(
                "Cervejas", "", "name_asc", 0
        );
    }

    @Test
    void deveRejeitarCategoriaInexistente() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> storeController.category(
                        "categoria-inexistente",
                        null,
                        "name_asc",
                        0,
                        model,
                        null
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(model);

        verify(productService, never())
                .findProductsByCategory(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt()
                );
    }

    @Test
    void deveRetornarListaVaziaParaCategoriaSemProdutos() {

        Page<Product> paginaVazia = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 12),
                0
        );

        when(productService.findProductsByCategory(
                "Gins", "", "name_asc", 0
        )).thenReturn(paginaVazia);

        String view = storeController.category(
                "gins",
                null,
                "name_asc",
                0,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(model).addAttribute(
                "categoryName",
                "Gins"
        );

        verify(model).addAttribute(
                "products",
                List.of()
        );

        verify(model).addAttribute(
                "totalPages",
                0
        );

        verify(model).addAttribute(
                "totalProducts",
                0L
        );

        verify(productService).findProductsByCategory(
                "Gins", "", "name_asc", 0
        );
    }

    @Test
    void deveCarregarHomepageComProdutosAtivos() {

        Product produto = new Product();
        produto.setName("Cerveja Heineken");
        produto.setActive(true);

        List<Product> produtos = List.of(produto);

        when(productService.findHomepageFeaturedProducts())
                .thenReturn(produtos);

        String view = storeController.home(model, null);

        assertEquals("client/homepage", view);

        verify(model).addAttribute(
                "products",
                produtos
        );

        verify(productService).findHomepageFeaturedProducts();

        verify(productRepository, never())
                .findByActiveTrueAndCategoryIgnoreCase(
                        anyString()
                );
    }

    @Test
    void deveCarregarQuantidadesDoCarrinhoParaClienteAutenticado() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CLIENT")
                        )
                );

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(10L);
        item1.setQuantity(2);

        CartItemDTO item2 = new CartItemDTO();
        item2.setProductId(15L);
        item2.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item1, item2));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        Model model = mock(Model.class);

        storeController.category(
                "cervejas",
                null,
                "name_asc",
                0,
                model,
                authentication
        );

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of(10L, 2, 15L, 3))
        );

        verify(cartService).getCart("cliente@email.com");
    }

    @Test
    void deveRetornarQuantidadesVaziasQuandoCarrinhoEstiverVazio() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CLIENT")
                        )
                );

        CartDTO cart = new CartDTO();
        cart.setItems(List.of());

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        Model model = mock(Model.class);

        storeController.category(
                "cervejas",
                null,
                "name_asc",
                0,
                model,
                authentication
        );

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );

        verify(cartService).getCart("cliente@email.com");
    }

    @Test
    void naoDeveConsultarCarrinhoParaVisitante() {

        Model model = mock(Model.class);

        storeController.category(
                "cervejas",
                null,
                "name_asc",
                0,
                model,
                null
        );

        verifyNoInteractions(cartService);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );
    }

    @Test
    void naoDeveConsultarCarrinhoParaAdministrador() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@email.com",
                        "senha",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        Model model = mock(Model.class);

        storeController.category(
                "cervejas",
                null,
                "name_asc",
                0,
                model,
                authentication
        );

        verifyNoInteractions(cartService);

        verify(model).addAttribute(
                eq("quantitiesByProductId"),
                eq(Map.of())
        );
    }

    @Test
    void deveCarregarQuantidadesDoCarrinhoNaHomepage() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@email.com",
                        "senha",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CLIENT")
                        )
                );

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Cerveja Heineken");
        produto.setActive(true);

        when(productService.findHomepageFeaturedProducts())
                .thenReturn(List.of(produto));

        CartItemDTO item = new CartItemDTO();
        item.setProductId(1L);
        item.setQuantity(3);

        CartDTO cart = new CartDTO();
        cart.setItems(List.of(item));

        when(cartService.getCart("cliente@email.com"))
                .thenReturn(cart);

        String view = storeController.home(
                model,
                authentication
        );

        assertEquals("client/homepage", view);

        verify(model).addAttribute(
                "products",
                List.of(produto)
        );

        verify(model).addAttribute(
                "quantitiesByProductId",
                Map.of(1L, 3)
        );

        verify(cartService).getCart("cliente@email.com");
    }

    @Test
    void deveEnviarDadosDaPaginacaoParaOTemplate() {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Cerveja Teste");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);

        Pageable pageable = PageRequest.of(1, 12);

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(produto),
                pageable,
                25
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq(""),
                eq("name_asc"),
                eq(1)
        )).thenReturn(paginaSimulada);

        String view = storeController.category(
                "cervejas",
                "",
                "name_asc",
                1,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(productService).findProductsByCategory(
                "Cervejas",
                "",
                "name_asc",
                1
        );

        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 3);
        verify(model).addAttribute("totalProducts", 25L);

        verify(model).addAttribute(
                "products",
                paginaSimulada.getContent()
        );

        verify(model).addAttribute("categoryName", "Cervejas");
        verify(model).addAttribute("sort", "name_asc");
    }

    @Test
    void devePreservarPesquisaDurantePaginacao() {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Heineken Long Neck");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);

        Pageable pageable = PageRequest.of(1, 12);

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(produto),
                pageable,
                13
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq("Heineken"),
                eq("name_asc"),
                eq(1)
        )).thenReturn(paginaSimulada);

        String view = storeController.category(
                "cervejas",
                "Heineken",
                "name_asc",
                1,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(productService).findProductsByCategory(
                "Cervejas",
                "Heineken",
                "name_asc",
                1
        );

        verify(model).addAttribute("keyword", "Heineken");

        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 2);
        verify(model).addAttribute("totalProducts", 13L);

        verify(model).addAttribute(
                "products",
                paginaSimulada.getContent()
        );

        verify(model).addAttribute("sort", "name_asc");
    }

    @Test
    void devePreservarOrdenacaoDurantePaginacao() {

        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Whisky Teste");
        produto.setCategory("Cervejas");
        produto.setActive(true);
        produto.setStock(10);

        Pageable pageable = PageRequest.of(1, 12);

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(produto),
                pageable,
                13
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq(""),
                eq("price_desc"),
                eq(1)
        )).thenReturn(paginaSimulada);

        String view = storeController.category(
                "cervejas",
                "",
                "price_desc",
                1,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(productService).findProductsByCategory(
                "Cervejas",
                "",
                "price_desc",
                1
        );

        verify(model).addAttribute("sort", "price_desc");

        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 2);
        verify(model).addAttribute("totalProducts", 13L);

        verify(model).addAttribute(
                "products",
                paginaSimulada.getContent()
        );
    }

    @Test
    void deveUtilizarOrdenacaoPadraoQuandoSortForInvalido() {

        Page<Product> paginaSimulada = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 12),
                0
        );

        when(productService.findProductsByCategory(
                eq("Cervejas"),
                eq(""),
                eq("name_asc"),
                eq(0)
        )).thenReturn(paginaSimulada);

        String view = storeController.category(
                "cervejas",
                "",
                "ordenacao_inexistente",
                0,
                model,
                null
        );

        assertEquals("client/category", view);

        verify(productService).findProductsByCategory(
                "Cervejas",
                "",
                "name_asc",
                0
        );

        verify(model).addAttribute("sort", "name_asc");

        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("totalPages", 0);
        verify(model).addAttribute("totalProducts", 0L);

        verify(model).addAttribute(
                "products",
                paginaSimulada.getContent()
        );
    }
}
