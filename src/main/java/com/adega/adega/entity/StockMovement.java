package com.adega.adega.entity;


import com.adega.adega.enumerated.StockMovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O produto é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull(message = "O tipo da movimentação é obrigatória.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockMovementType type;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser maior que zero.")
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres.")
    @Column(length = 255)
    private String reason;

    @Size(max = 255, message = "O nome de usuário deve ter no máximo 255 caracteres.")
    @Column(length =  255)
    private String userName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
        createdAt = LocalDateTime.now();
        }
    }

    //getters & setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Product getProduct() {return product;}

    public void setProduct(Product product) {this.product = product;}

    public Order getOrder() {return order;}

    public void setOrder(Order order) {this.order = order;}

    public StockMovementType getType() {return type;}

    public void setType(StockMovementType type) {this.type = type;}

    public Integer getQuantity() {return quantity;}

    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public String getReason() {return reason;}

    public void setReason(String reason) {this.reason = reason;}

    public String getUserName() {return userName;}

    public void setUserName(String userName) {this.userName = userName;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}


}


