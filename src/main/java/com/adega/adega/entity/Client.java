package com.adega.adega.entity;


import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números")
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 números")
    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Valid
    @Embedded
    private Address billingAddress;

    @Valid
    @ElementCollection
    @CollectionTable(
            name = "client_delivery_address",
            joinColumns = @JoinColumn(name = "client_id")
    )
    private List<Address> deliveryAddresses =  new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // constructors
    public Client() {

    }

    //getters & setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Users getUser() {return user;}

    public void setUser(Users user) {this.user = user;}

    public String getCpf() {return cpf;}

    public void setCpf(String cpf) {this.cpf = cpf;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}

    public Address getBillingAddress() {return billingAddress;}

    public void setBillingAddress(Address billingAddress) {this.billingAddress = billingAddress;}

    public List<Address> getDeliveryAddresses() {return deliveryAddresses;}

    public void setDeliveryAddresses(List<Address> deliveryAddresses) {this.deliveryAddresses = deliveryAddresses;}


}
