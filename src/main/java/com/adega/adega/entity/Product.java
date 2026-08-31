package com.adega.adega.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "A categoria é obrigatória")
    @Column(nullable = false, length = 100)
    private String category;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "O estoque é obrigatório")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    @Column(nullable = false)
    private Integer stock = 0;


    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_path")
    private String imagePath;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @NotNull(message = "O status do produto é obrigatório")
    @Column(nullable = false)
    private Boolean active = true;

    //constructors
    public Product() {
    }

    public Product (String name, String category, BigDecimal price,
                    Integer stock, Boolean active, String imageName, String imagePath, byte[] imageData) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageData = imageData;
    }

    //getters & setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public BigDecimal getPrice() {return price;}

    public void setPrice(BigDecimal price) {this.price = price;}

    public Integer getStock() {return stock;}

    public void setStock(Integer stock) {this.stock = stock;}

    public String getImageName() {return imageName;}

    public void setImageName(String imageName) {this.imageName = imageName;}

    public String getImagePath() {return imagePath;}

    public void setImagePath(String imagePath) {this.imagePath = imagePath;}

    public byte[] getImageData() {return imageData;}

    public void setImageData(byte[] imageData) {this.imageData = imageData;}

    public Boolean getActive() {return active;}

    public void setActive(Boolean active) {this.active = active;}
}
