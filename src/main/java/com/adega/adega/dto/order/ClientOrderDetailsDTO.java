package com.adega.adega.dto.order;

import com.adega.adega.enumerated.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ClientOrderDetailsDTO {

    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;

    private String deliveryCep;
    private String deliveryStreet;
    private String deliveryNumber;
    private String deliveryComplement;
    private String deliveryHood;
    private String deliveryCity;
    private String deliveryState;


    private List<OrderItemDTO> items;

    //getters && setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public LocalDateTime getOrderDate() {return orderDate;}

    public void setOrderDate(LocalDateTime orderDate) {this.orderDate = orderDate;}

    public OrderStatus getStatus() {return status;}

    public void setStatus(OrderStatus status) {this.status = status;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public String getDeliveryCep() {return deliveryCep;}

    public void setDeliveryCep(String deliveryCep) {this.deliveryCep = deliveryCep;}

    public String getDeliveryStreet() {return deliveryStreet;}

    public void setDeliveryStreet(String deliveryStreet) {this.deliveryStreet = deliveryStreet;}

    public String getDeliveryNumber() {return deliveryNumber;}

    public void setDeliveryNumber(String deliveryNumber) {this.deliveryNumber = deliveryNumber;}

    public String getDeliveryComplement() {return deliveryComplement;}

    public void setDeliveryComplement(String deliveryComplement) {this.deliveryComplement = deliveryComplement;}

    public String getDeliveryHood() {return deliveryHood;}

    public void setDeliveryHood(String deliveryHood) {this.deliveryHood = deliveryHood;}

    public String getDeliveryCity() {return deliveryCity;}

    public void setDeliveryCity(String deliveryCity) {this.deliveryCity = deliveryCity;}

    public String getDeliveryState() {return deliveryState;}

    public void setDeliveryState(String deliveryState) {this.deliveryState = deliveryState;}

    public List<OrderItemDTO> getItems() {return items;}

    public void setItems(List<OrderItemDTO> items) {this.items = items;}

}
