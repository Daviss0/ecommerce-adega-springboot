package com.adega.adega.service.impl;

import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.exception.OrderNotFoundException;
import com.adega.adega.mapper.OrderMapper;
import com.adega.adega.repository.OrderRepository;
import com.adega.adega.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    /*
    * ADMIN
    * */

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /*
    * CLIENT
    * */

    @Override
    @Transactional(readOnly = true)
    public List<ClientOrderSummaryDTO> findOrdersByClientEmail(String email) {

        List<Order> orders =
                orderRepository.findByClientUserEmailOrderByOrderDateDesc(email);

        return orderMapper.toSummaryDTOList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientOrderDetailsDTO findOrderDetailsByClientEmail(Long orderId, String email) {
        Order order = orderRepository.findByIdAndClientUserEmail(orderId, email)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado"));
        return orderMapper.toDetailsDTO(order);
    }
}
