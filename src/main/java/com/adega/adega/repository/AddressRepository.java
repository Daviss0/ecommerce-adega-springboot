package com.adega.adega.repository;

import com.adega.adega.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByClientIdOrderByPrincipalDescIdAsc(Long clientId);

    Optional<Address> findByIdAndClientId(Long id, Long clientId);

    Optional<Address> findByClientIdAndPrincipalTrue(Long clientId);

    boolean existsByClientId(Long clientId);

    long countByClientId(Long clientId);
}
