package com.adega.adega.repository;

import com.adega.adega.entity.Order;
import com.adega.adega.enumerated.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    //parte do cliente

    //Pedidos do cliente autenticado, do mais recente para o mais antigo
    List<Order> findByClientUserEmailOrderByOrderDateDesc(String email);

    //Busca um pedido específico garantindo que pertence ao cliente
    Optional<Order> findByIdAndClientUserEmail(Long id, String email);



    //parte administrativa

    //filtro utilizado pelo adm
    List<Order> findByStatus(OrderStatus status);

    //busca administrativa por nome do cliente
    @Query("""
        SELECT o
        FROM Order o
        WHERE LOWER(o.client.user.name)
        LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Order> searchOrders(String keyword);
}
