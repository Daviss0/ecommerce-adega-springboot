package com.adega.adega.service;

import com.adega.adega.dto.client.AddressDTO;

import java.util.List;

public interface AddressService {

    List<AddressDTO> findAllByClientEmail(String clientEmail);

    AddressDTO findByIdAndClientEmail(Long addressId, String email);

    void create(AddressDTO addressDTO, String clientEmail);

    void update(Long addressId, AddressDTO addressDTO, String clientEmail);

    void delete(Long addressid, String clientEmail);

    void setAsPrincipal(Long addressid, String clientEmail);
}
