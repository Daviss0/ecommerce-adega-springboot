package com.adega.adega.enumerated;

public enum OrderStatus {
    PENDING("Pedido recebido"),
    SHIPPED("Saiu para entrega"),
    DELIVERED("Entregue"),
    CANCELED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {return description;}
}
