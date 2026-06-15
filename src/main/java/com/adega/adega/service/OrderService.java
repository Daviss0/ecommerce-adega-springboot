package com.adega.adega.service;

import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;

import java.util.List;

public interface OrderService {

    List<Order> findAll();


    Order findById(Long id);

    List<Order> findByStatus(OrderStatus status);

    Order updateStatus(Long id, OrderStatus status);
}
