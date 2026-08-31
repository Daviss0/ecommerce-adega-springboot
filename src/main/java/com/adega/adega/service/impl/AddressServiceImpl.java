package com.adega.adega.service.impl;

import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.entity.Address;
import com.adega.adega.entity.Client;
import com.adega.adega.exception.AddressNotFoundException;
import com.adega.adega.exception.ClientNotFoundException;
import com.adega.adega.mapper.AddressMapper;
import com.adega.adega.repository.AddressRepository;
import com.adega.adega.repository.ClientRepository;
import com.adega.adega.service.AddressService;
import com.adega.adega.service.CepValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final AddressMapper addressMapper;
    private final CepValidationService cepValidationService;

    public AddressServiceImpl(AddressRepository addressReposiroty,
                              ClientRepository clientRepository,
                              AddressMapper addressMapper,
                              CepValidationService cepValidationService) {
        this.addressRepository = addressReposiroty;
        this.clientRepository = clientRepository;
        this.addressMapper = addressMapper;
        this.cepValidationService = cepValidationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> findAllByClientEmail(String clientEmail) {
        Client client = findClientByEmail(clientEmail);

        return addressRepository.findByClientIdOrderByPrincipalDescIdAsc(client.getId())
                .stream()
                .map(addressMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO findByIdAndClientEmail(Long addressId, String clientEmail) {
        Client client = findClientByEmail(clientEmail);

        Address address = findAddressByIdAndClient(addressId, client.getId());
        return addressMapper.toDTO(address);
    }

    @Override
    @Transactional
    public void create(AddressDTO addressDTO, String clientEmail) {
        Client client = findClientByEmail(clientEmail);
        cepValidationService.validateDeliveryArea(addressDTO.getCep());
        boolean clientAlreadyExists = addressRepository.existsByClientId(client.getId());

        Address address = addressMapper.toEntity(addressDTO);
        address.setClient(client);
        address.setPrincipal(!clientAlreadyExists);

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void update(Long addressId, AddressDTO addressDTO, String clientEmail) {
        Client client = findClientByEmail(clientEmail);

        Address address = findAddressByIdAndClient(addressId, client.getId());

        cepValidationService.validateDeliveryArea(addressDTO.getCep());

        addressMapper.updateEntity(addressDTO, address);

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void delete(Long addressId, String clientEmail){
        Client client = findClientByEmail(clientEmail);

        Address address = findAddressByIdAndClient(addressId, client.getId());

        boolean wasPrincipal = address.isPrincipal();

        addressRepository.delete(address);
        addressRepository.flush();

        if(wasPrincipal) {
            defineAnotherAddressAsPrincipal(client.getId());
        }
    }

    @Override
    @Transactional
    public void setAsPrincipal(Long addressId, String clientEmail) {
        Client client = findClientByEmail(clientEmail);

        Address newPrincipalAddress = findAddressByIdAndClient(addressId, client.getId());

        if (newPrincipalAddress.isPrincipal()) {
            return;
        }

        addressRepository.findByClientIdAndPrincipalTrue(client.getId())
                .ifPresent(currentPrincipal -> currentPrincipal.setPrincipal(false));
        newPrincipalAddress.setPrincipal(true);
    }


    //metodos auxiliares
    private Client findClientByEmail(String email) {
        return clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado"));
    }

    private Address findAddressByIdAndClient(Long addressId, Long clientId) {
        return addressRepository.findByIdAndClientId(addressId, clientId)
                .orElseThrow(() -> new AddressNotFoundException("Endereço não encontrado"));
    }

    private void defineAnotherAddressAsPrincipal(Long clientId) {
        addressRepository.findByClientIdOrderByPrincipalDescIdAsc(clientId)
                .stream()
                .findFirst()
                .ifPresent(address -> address.setPrincipal(true));
    }
}
