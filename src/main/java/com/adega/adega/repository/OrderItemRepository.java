package com.adega.adega.repository;

import com.adega.adega.entity.OrderItem;
import com.adega.adega.enumerated.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT oi.product.id, SUM(oi.quantity)
        FROM OrderItem oi
        WHERE oi.order.status = :status
          AND oi.product.active = true
          AND oi.product.stock > 0
          AND EXISTS (
              SELECT h.id
              FROM OrderStatusHistory h
              WHERE h.order = oi.order
                AND h.newStatus = :status
                AND h.changedAt >= :startDate
          )
        GROUP BY oi.product.id
        ORDER BY SUM(oi.quantity) DESC, oi.product.id ASC
        """)
    List<Object[]> findBestSellingProducts(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate
    );
}
