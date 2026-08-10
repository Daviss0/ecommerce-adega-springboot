package com.adega.adega.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 números")
    @Column(nullable = false, length = 8)
    private String cep;

    @NotBlank(message = "A rua é obrigatória")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "O número é obrigatório")
    @Column(nullable = false)
    private String number;

    private String complement;

    @NotBlank(message = "O bairro é orbigatório")
    @Column(nullable = false)
    private String hood;

    @NotBlank(message = "A cidadeé obrigatória")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // getters & setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getCep() {return cep;}

    public void setCep(String cep) {this.cep = cep;}

    public String getStreet() {return street;}

    public void setStreet(String street) {this.street = street;}

    public String getNumber() {return number;}

    public void setNumber(String number) {this.number = number;}

    public String getComplement() {return complement;}

    public void setComplement(String complement) {this.complement = complement;}

    public String getHood() {return hood;}

    public void setHood(String hood) {this.hood = hood;}

    public String getCity() {return city;}

    public void setCity(String city) {this.city = city;}

    public String getState() {return state;}

    public void setState(String state) {this.state = state;}

    public boolean isPrincipal() {return principal;}

    public void setPrincipal(boolean principal) {this.principal = principal;}

    public Client getClient() {return client;}

    public void setClient(Client client) {this.client = client;}


}
