package com.adega.adega.service;

import com.adega.adega.entity.Order;
import com.adega.adega.entity.Product;
import com.adega.adega.entity.StockMovement;

import java.util.List;

public interface StockService {

    List<Product> listProductsStock();

    void addStock(Long productId, Integer quantity, String reason, String userName);

    void removeStock(Long productId, Integer quantity, String reason, String userName, Order order);

    void removeStock(Long productId, Integer quantity, String reason, String userName);

    List<StockMovement> listMovements();

    List<StockMovement> listMovementsByProduct(Long productId);


}
