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

    Order markAsShipped(Long id, String userName);

    Order markAsDelivered(Long id, String userName);

    Order cancel(Long id, String userName);

    //CLIENT

    List<ClientOrderSummaryDTO> findOrdersByClientEmail(String email);

    ClientOrderDetailsDTO findOrderDetailsByClientEmail(Long orderId, String email);
}
