package com.adega.adega.service;

import com.adega.adega.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    Product findById(Long id);

    void updateProduct(Long id, Product product, MultipartFile imageFile);

    void deleteById(Long id);
}
