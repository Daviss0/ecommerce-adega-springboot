package com.adega.adega.dto.client;


import com.adega.adega.enumerated.ClientStatus;

import java.time.LocalDateTime;

public class ClientResponseDTO {

    private Long id;

    private String name;

    private String email;

    private String cpf;

    private String phone;

    private LocalDateTime createdAt;

    private AddressDTO billingAddress;

    public ClientResponseDTO() {
        this.billingAddress = new AddressDTO();
    }

    //getters && setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getCpf() {return cpf;}

    public void setCpf(String cpf) {this.cpf = cpf;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public AddressDTO getBillingAddress() {return billingAddress;}

    public void setBillingAddress(AddressDTO billingAddress) {this.billingAddress = billingAddress;}


}
