package com.adega.adega.mapper;


import com.adega.adega.dto.order.ClientOrderDetailsDTO;
import com.adega.adega.dto.order.ClientOrderSummaryDTO;
import com.adega.adega.dto.order.OrderItemDTO;
import com.adega.adega.entity.Order;
import com.adega.adega.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public ClientOrderSummaryDTO toSummaryDTO(Order order) {
        ClientOrderSummaryDTO dto = new ClientOrderSummaryDTO();

        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        int totalItems = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        dto.setTotalItems(totalItems);

        return dto;
    }

    public List<ClientOrderSummaryDTO> toSummaryDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    public ClientOrderDetailsDTO toDetailsDTO(Order order) {
        ClientOrderDetailsDTO dto = new ClientOrderDetailsDTO();

        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        List<OrderItemDTO> items = order.getItems()
                .stream()
                .map(this::toItemDTO)
                .toList();
        dto.setItems(items);

        return dto;
    }

    public OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();

        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }



    public List<OrderItemDTO> toItemDTOList(List<OrderItem> items) {
        return items.stream()
                .map(this::toItemDTO)
                .toList();
    }
}
