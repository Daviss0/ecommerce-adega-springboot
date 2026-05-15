package com.adega.adega.service;


import com.adega.adega.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    List<Client> findAllActive();

    List<Client> search(String keyword);

    Optional<Client> findById(Long id);

    void deactivateClient(Long id);
}
