package com.adega.adega.repository;


import com.adega.adega.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByClientUserEmail(String email);

    void deleteByUpdatedAtBefore(LocalDateTime dateTime);
}
