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

    private static final String VIA_CEP_URL =
            "https://viacep.com.br/ws/%s/json/";

    private static final String ALLOWED_STATE = "SP";
    private static final String ALLOWED_CITY = "São Paulo";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CepValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean isValid(String cep) {

        String normalizedCep = normalize(cep);

        if (!hasValidFormat(normalizedCep)) {
            return false;
        }

        ViaCepResponseDTO response =
                findCep(normalizedCep);

        return !response.notFound();
    }

    public void validateDeliveryArea(String cep) {

        String normalizedCep = normalize(cep);

        if (!hasValidFormat(normalizedCep)) {
            throw new IllegalArgumentException(
                    "Informe um CEP válido."
            );
        }

        ViaCepResponseDTO response =
                findCep(normalizedCep);

        if (response.notFound()) {
            throw new IllegalArgumentException(
                    "CEP não encontrado."
            );
        }

        if (!ALLOWED_STATE.equalsIgnoreCase(response.uf())) {
            throw new IllegalArgumentException(
                    "No momento realizamos entregas apenas em São Paulo - SP."
            );
        }

        if (!ALLOWED_CITY.equalsIgnoreCase(response.localidade())) {
            throw new IllegalArgumentException(
                    "No momento realizamos entregas apenas na cidade de São Paulo - SP."
            );
        }
    }

    private ViaCepResponseDTO findCep(String normalizedCep) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                VIA_CEP_URL.formatted(normalizedCep)
                        )
                )
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                throw new CepServiceUnavailableException(
                        "O serviço de consulta de CEP retornou um erro."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    ViaCepResponseDTO.class
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new CepServiceUnavailableException(
                    "A consulta do CEP foi interrompida.",
                    exception
            );

        } catch (IOException exception) {

            throw new CepServiceUnavailableException(
                    "Não foi possível consultar o CEP.",
                    exception
            );
        }
    }

    public String normalize(String cep) {

        if (cep == null) {
            return "";
        }

        return cep.replaceAll("\\D", "");
    }

    private boolean hasValidFormat(String cep) {
        return cep.matches("\\d{8}");
    }
}