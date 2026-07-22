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
        client.setCpf(dto.getCpf());
        client.setPhone(dto.getPhone());
        client.setBillingAddress(toAddress(dto.getBillingAddress()));
        return client;
    }

    //converte Client (entidade) em ClientResponseDTO
    public ClientResponseDTO toResponseDTO(Client client) {
        if(client == null) {
            return null;
        }

        ClientResponseDTO dto = new ClientResponseDTO();

        dto.setId(client.getId());
        dto.setCpf(client.getCpf());
        dto.setPhone(client.getPhone());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setBillingAddress(
                toAddressDTO(client.getBillingAddress())
        );

        if(client.getUser() != null) {
            dto.setName(client.getUser().getName());
            dto.setEmail(client.getUser().getEmail());
        }

        return dto;
    }

    //converte Client em ClientUpdateDTO para preencher o formulario de atualização de dados da conta
    public ClientUpdateDTO toUpdateDTO(Client client) {
        if(client == null) {
            return null;
        }

        ClientUpdateDTO dto = new ClientUpdateDTO();

        dto.setPhone(client.getPhone());
        dto.setBillingAddress(
                toAddressDTO(client.getBillingAddress())
        );

        if (client.getUser() != null) {
            dto.setName(client.getUser().getName());
            dto.setEmail(client.getUser().getEmail());
        }
        return dto;
    }

    //atualiza as entidades existentes com os dados permitidos enviados pelo cliente
    public void updateEntities (
            ClientUpdateDTO dto,
            Client client
    ) {
        if(dto == null || client == null) {
            return;
        }

        client.setPhone(dto.getPhone());

        if(client.getUser() == null) {
            client.getUser().setName(dto.getName());
            client.getUser().setEmail(dto.getEmail());
        }

        if(client.getBillingAddress() == null) {
            client.setBillingAddress(
                    toAddress(dto.getBillingAddress())
            );
        }
        else {
            updateAddress(
                    dto.getBillingAddress(),
                    client.getBillingAddress()
            );
        }
    }


    //converte AddressDTO mem Address
    public Address toAddress(AddressDTO dto) {
        if(dto == null){
            return null;
        }

        Address address = new Address();

        address.setCep(dto.getCep());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setHood(dto.getHood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());

        return address;
    }

    //converte Address em AddressDTO
    public AddressDTO toAddressDTO(Address address) {
        if(address == null) {
            return null;
        }
        AddressDTO dto = new AddressDTO();

        dto.setCep(address.getCep());
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setComplement(address.getComplement());
        dto.setHood(address.getHood());
        dto.setCity(address.getCity());
        dto.setState(address.getState());

        return dto;
    }


    //atualiza um endereço existente sem substituir
    //desnecessariamente o objeto embutido
    private void updateAddress(
            AddressDTO dto,
            Address address
    ) {
        if(dto == null || address == null) {
            return;
        }

        address.setCep(dto.getCep());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setHood(dto.getHood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());

    }

}
