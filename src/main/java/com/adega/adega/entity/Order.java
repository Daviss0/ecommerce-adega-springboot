package com.adega.adega.entity;

import com.adega.adega.enumerated.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "O status do pedido é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor tem que ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    //atributos de endereço

    @NotBlank(message = "O CEP de entrega é obrigatório")
    @Column(nullable = false, length = 8)
    private String deliveryCep;

    @NotBlank(message = "A rua de entrega é obrigatória")
    @Column(nullable = false)
    private String deliveryStreet;

    @NotBlank(message = "O número de entrega é obrigatório")
    @Column(nullable = false)
    private String deliveryNumber;

    private String deliveryComplement;

    @NotBlank(message = "O bairro de entrega é obrigatório")
    @Column(nullable = false)
    private String deliveryHood;

    @NotBlank(message = "A cidade de entrega é obrigatória")
    @Column(nullable = false)
    private String deliveryCity;

    @NotBlank(message = "O estado de entrega é obrigatório")
    @Column(nullable = false)
    private String deliveryState;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if(orderDate == null) {
            orderDate = LocalDateTime.now();
        }

        if(status == null) {
            status = OrderStatus.PENDING;
        }

        if(totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    //metodo auxiliar
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    //getters & setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public LocalDateTime getOrderDate() {return orderDate;}

    public void setOrderDate(LocalDateTime orderDate) {this.orderDate = orderDate;}

    public Client getClient() {return client;}

    public void setClient(Client client) {this.client = client;}

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

    public List<OrderItem> getItems() {return items;}

    public void setItems(List<OrderItem> items) {
        this.items.clear();

        if (items != null) {
            items.forEach(this::addItem);
        }
    }
}
