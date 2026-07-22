package com.adega.adega.service;

import com.adega.adega.dto.integration.ViaCepResponseDTO;
import com.adega.adega.exception.CepServiceUnavailableException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class CepValidationService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/%s/json/";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CepValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public boolean isValid(String cep) {
        String normalizedCep = normalize(cep);

        if(!hasValidFormat(normalizedCep)) {
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create( VIA_CEP_URL.formatted(normalizedCep)))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                throw new CepServiceUnavailableException("O serviço de consulta de CEP retornou um erro.");
            }

            ViaCepResponseDTO viaCepResponse = objectMapper.readValue(response.body(), ViaCepResponseDTO.class);

            return !viaCepResponse.notFound();
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new CepServiceUnavailableException("A consulta do CEP foi interrompida.", exception);
        }
        catch (IOException exception) {
            throw new CepServiceUnavailableException("Não foi possível consultar o CEP.", exception);
        }
    }

    public String normalize(String cep) {
     if(cep == null){
         return "";
     }
     return cep.replaceAll("\\D", "");
    }

    private boolean hasValidFormat(String cep) {
        return cep.matches("\\d{8}");
    }
}
