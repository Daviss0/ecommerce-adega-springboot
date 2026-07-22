package com.adega.adega.service;


import com.adega.adega.dto.client.ClientRegistrationDTO;
import com.adega.adega.dto.client.ClientResponseDTO;
import com.adega.adega.dto.client.ClientUpdateDTO;
import com.adega.adega.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService {


    //metodos administrativos
    List<Client> findAllActive();

    List<Client> search(String keyword);

    Optional<Client> findById(Long id);

    void deactivateClient(Long id);

    //metodos usados na area do cliente
    ClientResponseDTO register(ClientRegistrationDTO dto);

    ClientResponseDTO findByEmail(String email);

    ClientUpdateDTO getUpdateData(String email);

    ClientResponseDTO update(
            String currentEmail,
            ClientUpdateDTO dto
    );

}
