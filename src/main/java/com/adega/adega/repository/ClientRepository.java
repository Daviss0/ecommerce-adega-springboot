package com.adega.adega.repository;

import com.adega.adega.entity.Client;
import com.adega.adega.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository <Client, Long> {

    Optional<Client> findByCpf(String cpf);

    Optional<Client> findByUser(User user);

    List<Client> findByUser_ActiveTrue();

    List<Client> findByUser_NameContainingIgnoreCase(String name);

    List<Client> findByUser_EmailContainingIgnoreCase(String email);

    List<Client> findByCpfContaining(String cpf);

    Optional<Client> findByUser_Email(String email);

    boolean existsByCpf(String cpf);
}
