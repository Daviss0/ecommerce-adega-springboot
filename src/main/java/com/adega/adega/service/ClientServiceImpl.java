package com.adega.adega.service;


import com.adega.adega.entity.Client;
import com.adega.adega.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService{


    final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Client> findAllActive() {
        return clientRepository.findByUser_ActiveTrue();
    }

    @Override
    public List<Client> search(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return findAllActive();
        }

        List<Client> byName = clientRepository.findByUser_NameContainingIgnoreCase(keyword);
        List<Client> byEmail = clientRepository.findByUser_EmailContainingIgnoreCase(keyword);
        List<Client> byCpf = clientRepository.findByCpfContaining(keyword);

        byName.addAll(byEmail);
        byName.addAll(byCpf);

        return byName.stream()
                .distinct()
                .filter(client -> Boolean.TRUE.equals(client.getUser().getActive()))
                .toList();
    }

    @Override
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public void deactivateClient(Long id) {
      Client client = clientRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

      client.getUser().setActive(false);
      clientRepository.save(client);
    }
}
