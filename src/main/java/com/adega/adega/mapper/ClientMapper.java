package com.adega.adega.mapper;

import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.dto.client.ClientRegistrationDTO;
import com.adega.adega.dto.client.ClientResponseDTO;
import com.adega.adega.dto.client.ClientUpdateDTO;
import com.adega.adega.entity.Address;
import com.adega.adega.entity.Client;
import com.adega.adega.entity.User;
import com.adega.adega.enumerated.Role;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {


    //converte os dados do cadastro em uma entidade user
    public User toUser(
            ClientRegistrationDTO dto,
            String encodedPassword
    ) {
        if(dto == null) {
            return null;
        }

        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(Role.CLIENT);
        user.setActive(true);

        return user;
    }


    //converte os dados especificos do cliente em uma entidade client
    public Client toClient(
            ClientRegistrationDTO dto,
            User user
    ) {
        if(dto == null) {
            return null;
        }

        Client client = new Client();

        client.setUser(user);
        client.setPhone(dto.getPhone());
        return client;
    }

    //converte Client (entidade) em ClientResponseDTO
    public ClientResponseDTO toResponseDTO(Client client) {
        if (client == null) {
            return null;
        }

        ClientResponseDTO dto = new ClientResponseDTO();

        dto.setId(client.getId());
        dto.setPhone(client.getPhone());
        dto.setCreatedAt(client.getCreatedAt());

        if (client.getUser() != null) {
            dto.setName(client.getUser().getName());
            dto.setEmail(client.getUser().getEmail());
        }

        return dto;
    }

    //converte Client em ClientUpdateDTO para preencher o formulario de atualização de dados da conta
    public ClientUpdateDTO toUpdateDTO(Client client) {
        if (client == null) {
            return null;
        }

        ClientUpdateDTO dto = new ClientUpdateDTO();

        dto.setPhone(client.getPhone());

        if (client.getUser() != null) {
            dto.setName(client.getUser().getName());
            dto.setEmail(client.getUser().getEmail());
        }

        return dto;
    }

    //atualiza as entidades existentes com os dados permitidos enviados pelo cliente
    public void updateEntities(
            ClientUpdateDTO dto,
            Client client
    ) {
        if (dto == null || client == null) {
            return;
        }

        client.setPhone(dto.getPhone());

        if (client.getUser() != null) {
            client.getUser().setName(dto.getName());
            client.getUser().setEmail(dto.getEmail());
        }
    }

}
