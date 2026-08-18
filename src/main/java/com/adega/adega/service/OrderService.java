package com.adega.adega.service;

import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;

import java.util.List;

public interface OrderService {

    //ADMIN
    List<Order> findAll();

    Order findById(Long id);

    List<Order> findByStatus(OrderStatus status);

    Order updateStatus(Long id, OrderStatus status);

    //CLIENT

    List<ClientOrderSummaryDTO> findOrdersByClientEmail(String email);

    ClientOrderDetailsDTO findOrderDetailsByClientEmail(Long orderId, String email);
}
