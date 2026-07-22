package com.adega.adega.dto.client;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressDTO {

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(
            regexp = "\\d{8}",
            message = "O CEP deve possuir 8 números"
    )
    private String cep;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 150, message = "O endereço deve possuir no máximo 150 caracteres")
    private String street;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 20, message = "O número deve possuir no máximo 20 caracteres")
    private String number;

    @Size(max = 100, message = "O complemento deve possuir no máximo 100 caracteres")
    private String complement;

    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 100, message = "O bairro deve possuir no máximo 100 caracteres")
    private String hood;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres")
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    @Pattern(
            regexp = "^[A-Z]{2}$",
            message = "Informe um estado válido"
    )
    private String state;

    //getters && setters
    public  String getCep() {return cep;}

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

    public String getState (){return state;}

    public void setState(String state) {this.state = state;}


}
