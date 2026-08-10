package com.adega.adega.mapper;

import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressDTO dto) {
        if(dto == null) {
            return null;
        }

        Address address = new Address();

        address.setCep(normalizeCep(dto.getCep()));
        address.setStreet(normalizeText(dto.getStreet()));
        address.setNumber(normalizeText(dto.getNumber()));
        address.setComplement(normalizeOptionalText(dto.getComplement()));
        address.setHood(normalizeText(dto.getHood()));
        address.setCity(normalizeText(dto.getCity()));
        address.setState("SP");

        return address;
    }

    public AddressDTO toDTO(Address address) {
        if(address == null) {
            return null;
        }

        AddressDTO dto = new AddressDTO();

        dto.setId(address.getId());
        dto.setCep(address.getCep());
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setComplement(address.getComplement());
        dto.setHood(address.getHood());
        dto.setCity(address.getCity());
        dto.setPrincipal(address.isPrincipal());

        return dto;
        }

        public void updateEntity(AddressDTO dto, Address address) {
        if(dto == null || address == null) {
            return;
        }

        address.setCep(normalizeCep(dto.getCep()));
        address.setStreet(normalizeText(dto.getStreet()));
        address.setNumber(normalizeText(dto.getNumber()));
        address.setComplement(normalizeOptionalText(dto.getComplement()));
        address.setHood(normalizeText(dto.getHood()));
        address.setCity(normalizeText(dto.getCity()));
        address.setState("SP");
        }

    private String normalizeCep(String cep) {
        if(cep == null) {
            return null;
        }
        return cep.replaceAll("\\D", "");
    }

    private String normalizeText(String value) {
        if(value == null) {
            return null;
        }
        return value.trim();
        }


    private String normalizeOptionalText(String value) {
    if(value == null || value.isBlank()) {
        return null;
    }
    return value.trim();
    }
}
