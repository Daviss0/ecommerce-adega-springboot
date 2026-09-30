package com.adega.adega.repository;


import com.adega.adega.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByClientUserEmail(String email);

    @Query("""
        SELECT DISTINCT c
        FROM Cart c
        JOIN FETCH c.items
        WHERE c.updatedAt < :limitDate
        AND c.items IS NOT EMPTY
        """)
    List<Cart> findAbandonedCarts(@Param("limitDate") LocalDateTime limitDate);}
