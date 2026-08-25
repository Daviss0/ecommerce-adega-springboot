package com.adega.adega.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartDTO {

    @NotNull(message = "O produto é obrigatório.")
    private Long productId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade mínima é 1.")
    private Integer quantity;

    public AddCartDTO() {
    }

    public AddCartDTO(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    //getters && setters
    public Long getProductId() {return productId;}

    public void setProductId(Long productId) {this.productId = productId;}

    public Integer getQuantity() {return quantity;}

    public void setQuantity(Integer quantity) {this.quantity = quantity;}


}
