package com.adega.adega.service.impl;

import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.entity.Order;
import com.adega.adega.entity.OrderStatusHistory;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.exception.InvalidOrderStatusTransitionException;
import com.adega.adega.exception.OrderNotFoundException;
import com.adega.adega.mapper.OrderMapper;
import com.adega.adega.repository.OrderRepository;
import com.adega.adega.repository.OrderStatusHistoryRepository;
import com.adega.adega.service.OrderService;
import com.adega.adega.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StockService stockService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;


    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper,
                            StockService stockService, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.stockService = stockService;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    /*
    * ADMIN
    * */

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByOrderDateDesc();
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
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    @Override
    @Transactional
    public Order markAsShipped(Long id, String userName) {
        return changeStatus(id, OrderStatus.SHIPPED, userName);
    }

    @Override
    @Transactional
    public Order markAsDelivered(Long id, String userName) {
        return changeStatus(id, OrderStatus.DELIVERED, userName);
    }

    @Override
    @Transactional
    public Order cancel(Long id, String userName) {

        Order order = orderRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado."));

        OrderStatus previousStatus = order.getStatus();

        validateStatusTransition(previousStatus, OrderStatus.CANCELED);

        order.getItems().forEach(item -> {

            stockService.addStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "Devolução de estoque por cancelamento do pedido #" + order.getId(),
                    userName,
                    order
            );
        });

        order.setStatus(OrderStatus.CANCELED);

        Order savedOrder = orderRepository.save(order);

        registerStatusHistory(savedOrder, previousStatus, OrderStatus.CANCELED, userName);

        return savedOrder;
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

    //metodo auxiliar
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {

        boolean validTransition = switch (currentStatus) {
            case PENDING ->
                    newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;

            case SHIPPED ->
                newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELED -> false;
        };

        if (!validTransition) {
            throw new InvalidOrderStatusTransitionException("" +
                    "Não é possível alterar o pedido de '" + currentStatus.getDescription() + "' para '"
            + newStatus.getDescription() + "'.");
        }
    }

    private Order changeStatus(Long id, OrderStatus newStatus, String userName) {

        Order order = findById(id);

        OrderStatus previousStatus = order.getStatus();

        validateStatusTransition(previousStatus, newStatus);

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);

        registerStatusHistory(savedOrder, previousStatus, newStatus, userName);

        return savedOrder;
    }

    private void registerStatusHistory(Order order, OrderStatus previousStatus,
                                       OrderStatus newStatus, String changedBy) {

        OrderStatusHistory history = new OrderStatusHistory();

        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);

        orderStatusHistoryRepository.save(history);
    }

}
