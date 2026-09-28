package com.adega.adega.entity;


import com.adega.adega.enumerated.OrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    //getters && setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Order getOrder() {return order;}

    public void setOrder(Order order) {this.order = order;}

    public OrderStatus getPreviousStatus() {return previousStatus;}

    public void setPreviousStatus(OrderStatus previousStatus) {this.previousStatus = previousStatus;}

    public OrderStatus getNewStatus() {return newStatus;}

    public void setNewStatus(OrderStatus newStatus) {this.newStatus = newStatus;}

    public LocalDateTime getChangedAt() {return changedAt;}

    public void setChangedAt(LocalDateTime changedAt) {this.changedAt = changedAt;}

    public String getChangedBy() {return changedBy;}

    public void setChangedBy(String changedBy) {this.changedBy = changedBy;}

}
