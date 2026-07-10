package com.adega.adega.service.impl;


import com.adega.adega.entity.Product;
import com.adega.adega.entity.StockMovement;
import com.adega.adega.enumerated.StockMovementType;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.repository.StockMovementRepository;
import com.adega.adega.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockServiceImpl(StockMovementRepository stockMovementRepository, ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> listProductsStock() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public void addStock(Long productId, Integer quantity, String reason, String userName) {

        if(quantity == null || quantity <= 0) {
            throw new RuntimeException("A quantidade deve ser maior que zero.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(StockMovementType.ENTRADA)
                .quantity(quantity)
                .reason(reason)
                .userName(userName)
                .build();

        stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public void removeStock(Long productId, Integer quantity, String reason, String userName) {

        if(quantity == null || quantity <= 0) {
            throw new RuntimeException("A quantidade deve ser maior que zero.");
        }

        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if(product.getStock() < quantity) {
            throw new RuntimeException("Estoque insuficiente para realizar a saída.");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(StockMovementType.SAIDA)
                .quantity(quantity)
                .reason(reason)
                .userName(userName)
                .build();

        stockMovementRepository.save(movement);
    }

    @Override
    public List<StockMovement> listMovements() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<StockMovement> listMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
}
