package com.adega.adega.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponseDTO (
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
){

    public boolean notFound() {
        return Boolean.TRUE.equals(erro);
    }
}
