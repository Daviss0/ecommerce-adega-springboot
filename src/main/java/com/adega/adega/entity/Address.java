package com.adega.adega.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Embeddable
public class Address {

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 números")
    private String cep;

    @NotBlank(message = "A rua é obrigatória")
    private String street;

    @NotBlank(message = "O número é obrigatório")
    private String number;

    private String complement;

    @NotBlank(message = "O bairro é orbigatório")
    private String hood;

    @NotBlank(message = "A cidadeé obrigatória")
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    private String state;

    // getters & setters

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


}
