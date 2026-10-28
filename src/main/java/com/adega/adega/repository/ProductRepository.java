package com.adega.adega.repository;

import com.adega.adega.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryContainingIgnoreCase(String category);

    List<Product> findByActiveTrue();

    List<Product> findByActiveTrueAndCategoryIgnoreCase(String category);

    List<Product> findByActiveTrueAndStockGreaterThanAndNameContainingIgnoreCaseOrderByNameAsc(
            Integer stock,
            String name
    );


    Page<Product> findByActiveTrueAndStockGreaterThanAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
            Integer stock,
            String category,
            String keyword,
            Pageable pageable
    );

}
