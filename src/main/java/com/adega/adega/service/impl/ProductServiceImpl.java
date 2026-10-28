package com.adega.adega.service.impl;

import com.adega.adega.entity.Product;
import com.adega.adega.exception.ProductImageException;
import com.adega.adega.exception.ProductNotFoundException;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.adega.adega.util.ProductCategories;
import java.util.List;
import com.adega.adega.repository.OrderItemRepository;
import com.adega.adega.enumerated.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.text.Collator;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

import java.io.IOException;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductServiceImpl(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado."));
    }

    @Override
    public void updateProduct(Long id, Product updatedProduct, MultipartFile imageFile) {
        Product existingProduct = findById(id);

        if (!ProductCategories.isValid(updatedProduct.getCategory())) {
            throw new IllegalArgumentException("Selecione uma categoria válida.");
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setActive(updatedProduct.getActive());

        if(imageFile != null && !imageFile.isEmpty()) {
            try{
                existingProduct.setImageName(imageFile.getOriginalFilename());
                existingProduct.setImageData(imageFile.getBytes());
            }

            catch (IOException e) {
                throw new ProductImageException(
                        "Não foi possível processar a imagem do produto.",e);
            }
        }
        productRepository.save(existingProduct);
    }

    @Override
    public void deleteById(Long id) {
        Product product = findById(id);

        productRepository.delete(product);
    }



    @Override
    public List<Product> findHomepageFeaturedProducts() {

        final int MAX_FEATURED_PRODUCTS = 8;

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        List<Object[]> bestSellingProducts =
                orderItemRepository.findBestSellingProducts(
                        OrderStatus.DELIVERED,
                        startDate
                );

        List<Product> featuredProducts = new ArrayList<>();
        Set<Long> selectedProductIds = new HashSet<>();

        for (Object[] row : bestSellingProducts) {

            if (featuredProducts.size() >= MAX_FEATURED_PRODUCTS) {
                break;
            }

            Long productId = ((Number) row[0]).longValue();

            productRepository.findById(productId)
                    .filter(product ->
                            Boolean.TRUE.equals(product.getActive())
                                    && product.getStock() != null
                                    && product.getStock() > 0
                    )
                    .ifPresent(product -> {
                        if (selectedProductIds.add(product.getId())) {
                            featuredProducts.add(product);
                        }
                    });
        }

        if (featuredProducts.size() < MAX_FEATURED_PRODUCTS) {

            Collator collator = Collator.getInstance(
                    Locale.forLanguageTag("pt-BR")
            );

            collator.setStrength(Collator.PRIMARY);

            List<Product> availableProducts =
                    productRepository.findByActiveTrue()
                            .stream()
                            .filter(product ->
                                    product.getStock() != null
                                            && product.getStock() > 0
                            )
                            .sorted(
                                    Comparator.comparing(
                                            Product::getName,
                                            collator
                                    ).thenComparing(Product::getId)
                            )
                            .toList();

            for (Product product : availableProducts) {

                if (featuredProducts.size() >= MAX_FEATURED_PRODUCTS) {
                    break;
                }

                if (selectedProductIds.add(product.getId())) {
                    featuredProducts.add(product);
                }
            }
        }

        return featuredProducts;
    }

    @Override
    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String normalizedKeyword = keyword.trim();

        return productRepository.findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(0, normalizedKeyword);
    }

    @Override
    public Page<Product> findProductsByCategory(String category, String keyword, String sort, int page) {

        final int PAGE_SIZE = 12;

        int validPage = Math.max(page, 0);

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        Sort sorting = switch (sort == null ? "" : sort) {

            case "name_desc" -> JpaSort.unsafe(
                    Sort.Direction.DESC, "LOWER(name)"
            ).and(Sort.by("id"));

            case "price_asc" -> Sort.by(
                    Sort.Direction.ASC, "price"
            ).and(Sort.by("id"));

            case "price_desc" -> Sort.by(
                    Sort.Direction.DESC, "price"
            ).and(Sort.by("id"));

            default -> JpaSort.unsafe(
                    Sort.Direction.ASC, "LOWER(name)"
            ).and(Sort.by("id"));
        };

        Pageable pageable = PageRequest.of(validPage, PAGE_SIZE, sorting);

        return productRepository.findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                0, category, normalizedKeyword, pageable
        );
    }


}
