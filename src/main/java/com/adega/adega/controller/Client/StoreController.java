package com.adega.adega.controller.Client;

import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.adega.adega.util.ProductCategories;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.adega.adega.service.CartService;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.adega.adega.service.ProductService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/store")
public class StoreController {

    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ProductService productService;

    public StoreController (ProductRepository productRepository, CartService cartService,  ProductService productService) {
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.productService = productService;

    }

    @GetMapping({"", "/", "/homepage"})
    public String home(Model model, Authentication authentication) {

        model.addAttribute("products", productService.findHomepageFeaturedProducts());

        Map<Long, Integer> quantitiesByProductId = Map.of();

        if (authentication != null && authentication.isAuthenticated()
            && authentication.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_CLIENT"))) {

            CartDTO cart = cartService.getCart(authentication.getName());

            quantitiesByProductId = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItemDTO::getProductId,
                            CartItemDTO::getQuantity
                    ));
        }

        model.addAttribute("quantitiesByProductId", quantitiesByProductId);
        return "client/homepage";
    }

    @GetMapping("/products/image/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImageData());
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "name_asc") String sort,
                           @RequestParam(defaultValue = "0") int page,
                           Model model, Authentication authentication) {

        String category = ProductCategories.fromSlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria não encontrada"
                ));

        String normalizedKeyword = keyword == null ? "" :keyword.trim();

        String normalizedSort = switch (sort) {
            case "name_desc", "price_asc", "price_desc" -> sort;
            default -> "name_asc";
        };

        Page<Product> productPage = productService.findProductsByCategory(
                category, normalizedKeyword, normalizedSort, page
        );

        model.addAttribute("categoryName", category);
        model.addAttribute("categorySlug", ProductCategories.toSlug(category));

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("sort", normalizedSort);

        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalProducts", productPage.getTotalElements());

        Map<Long, Integer> quantitiesByProductId = Map.of();

        if (authentication != null && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT"))) {

            CartDTO cart = cartService.getCart(authentication.getName());

            quantitiesByProductId = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItemDTO::getProductId,
                            CartItemDTO::getQuantity
                    ));
        }

        model.addAttribute("quantitiesByProductId", quantitiesByProductId);

        return "client/category";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model, Authentication authentication) {

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        model.addAttribute("products", productService.searchProducts(normalizedKeyword));
        model.addAttribute("keyword", normalizedKeyword);

        Map<Long, Integer> quantitiesByProductId = Map.of();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT"))) {

            CartDTO cart = cartService.getCart(authentication.getName());

            quantitiesByProductId = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItemDTO::getProductId,
                            CartItemDTO::getQuantity
                    ));
        }
            model.addAttribute("quantitiesByProductId", quantitiesByProductId);

            return "client/search";
        }



    }

