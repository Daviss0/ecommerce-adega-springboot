package com.adega.adega.dto.order;


import com.adega.adega.enumerated.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClientOrderSummaryDTO {

    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Integer totalItems;


    //getters && setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public LocalDateTime getOrderDate() {return orderDate;}

    public void setOrderDate(LocalDateTime orderDate) {this.orderDate = orderDate;}

    public OrderStatus getStatus() {return status;}

    public void setStatus(OrderStatus status) {this.status = status;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public Integer getTotalItems() {return totalItems;}

    public void setTotalItems(Integer totalItems) {this.totalItems = totalItems;}
}
