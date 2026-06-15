package com.adega.adega.repository;

import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);


    @Query("""
    SELECT o
    FROM Order o
    WHERE LOWER(o.client.user.name)
    LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Order> searchOrders(String keyword);
}
