package com.adega.adega.service.impl;


import com.adega.adega.dto.client.ClientRegistrationDTO;
import com.adega.adega.dto.client.ClientResponseDTO;
import com.adega.adega.dto.client.ClientUpdateDTO;
import com.adega.adega.entity.Client;
import com.adega.adega.entity.User;
import com.adega.adega.mapper.ClientMapper;
import com.adega.adega.repository.ClientRepository;
import com.adega.adega.repository.UserRepository;
import com.adega.adega.service.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {


    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository clientRepository,
                             UserRepository userRepository,
                             ClientMapper clientMapper,
                             PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.clientMapper = clientMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAllActive() {
        return clientRepository.findByUser_ActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> search(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return findAllActive();
        }

       String normalizedKeyword = keyword.trim();

        List<Client> byName =
                clientRepository.findByUser_NameContainingIgnoreCase(
                        normalizedKeyword
                );

        List<Client> byEmail =
                clientRepository.findByUser_EmailContainingIgnoreCase(
                        normalizedKeyword
                );


        List<Client> results = new ArrayList<>(byName);

       results.addAll(byEmail);

       return results.stream()
               .distinct()
               .filter(client ->
                       client.getUser() != null
               && Boolean.TRUE.equals(client.getUser().getActive()))
               .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    @Transactional
    public void deactivateClient(Long id) {
      Client client = clientRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

      User user = client.getUser();

      if(user == null) {
          throw new RuntimeException("Usuário relacionado ao cliente não encontrado");
      }

      user.setActive(false);

      userRepository.save(user);
    }


    @Override
    @Transactional
    public ClientResponseDTO register(ClientRegistrationDTO dto) {
        validateRegistration(dto);

        String normalizedEmail = dto.getEmail().trim().toLowerCase();


        String normalizedPhone = onlyNumbers(dto.getPhone());

        if(userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com esse e-mail"
            );
        }


        dto.setEmail(normalizedEmail);
        dto.setPhone(normalizedPhone);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = clientMapper.toUser(dto, encodedPassword);

        User savedUser = userRepository.save(user);

        Client client = clientMapper.toClient(dto, savedUser);

        Client savedClient = clientRepository.save(client);

        return clientMapper.toResponseDTO(savedClient);
    }


    //exibição da pagina minha conta
    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO findByEmail(String email) {
        Client client = findClientByEmail(email);

        return clientMapper.toResponseDTO(client);
    }


    //preenchimento do formulario de edição
    @Override
    @Transactional(readOnly = true)
    public ClientUpdateDTO getUpdateData(String email) {
        Client client = findClientByEmail(email);

        return clientMapper.toUpdateDTO(client);
    }


    //atualização da conta do cliente
    @Override
    @Transactional
    public ClientResponseDTO update (String currentEmail, ClientUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Os dados de atualização são obrigatórios");
        }

        Client client = findClientByEmail(currentEmail);

        String normalizedNewEmail = dto.getEmail().trim().toLowerCase();

        String normalizedPhone = onlyNumbers(dto.getPhone());

        validateEmailChange(currentEmail, normalizedNewEmail);

        dto.setEmail(normalizedNewEmail);
        dto.setPhone(normalizedPhone);

        clientMapper.updateEntities(dto, client);

        userRepository.save(client.getUser());

        Client updatedClient = clientRepository.save(client);

        return clientMapper.toResponseDTO(updatedClient);
    }


    //metodos privados auxiliares

    private Client findClientByEmail(String email) {
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail não informado");
        }

        String normalizedEmail = email.trim().toLowerCase();

        return clientRepository.findByUser_Email(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    private void validateRegistration(ClientRegistrationDTO dto) {
      if (dto == null) {
          throw new IllegalArgumentException("Os dados do cadastro são obrigatórios");
      }
      validatePasswords(dto);


    }

    private void validatePasswords (ClientRegistrationDTO dto) {
        if(dto.getPassword() == null || dto.getConfirmPassword() == null
                || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("A senha e a confirmação de senha não coincidem");
        }
    }



    private void validateEmailChange(String currentEmail, String newEmail) {

        if (currentEmail.equalsIgnoreCase(newEmail)) {
            return;
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("Já existe usuário cadastrado com esse e-mail");
        }
    }

    private String onlyNumbers(String value) {
        if(value == null) {
            return null;
        }

        return value.replaceAll("\\D", "");
    }
}
