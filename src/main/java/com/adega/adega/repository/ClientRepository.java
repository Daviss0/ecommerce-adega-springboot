package com.adega.adega.repository;

import com.adega.adega.entity.Client;
import com.adega.adega.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository <Client, Long> {

    Optional<Client> findByCpf(String cpf);

    Optional<Client> findByUser(Users user);

    List<Client> findByUser_ActiveTrue();

    List<Client> findByUser_NameContainingIgnoreCase(String name);

    List<Client> findByUser_EmailContainingIgnoreCase(String email);

    List<Client> findByCpfContaining(String cpf);
}
