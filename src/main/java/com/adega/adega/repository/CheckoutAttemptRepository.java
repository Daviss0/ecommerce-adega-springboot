package com.adega.adega.repository;


import com.adega.adega.entity.CheckoutAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckoutAttemptRepository extends JpaRepository<CheckoutAttempt, Long> {

    @Modifying
    @Query("""
        UPDATE CheckoutAttempt ca
           SET ca.used = true
         WHERE ca.token = :token
           AND ca.client.id = :clientId
           AND ca.used = false
    """)
    int consumeToken(
            @Param("token") String token,
            @Param("clientId") Long clientId
    );
}
