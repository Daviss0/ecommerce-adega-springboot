package com.adega.adega.service.impl;

import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Override
    public void updateProduct(Long id, Product updatedProduct, MultipartFile imageFile) {
        Product existingProduct = findById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setStatus(updatedProduct.getStatus());

        if(imageFile != null && !imageFile.isEmpty()) {
            try{
                existingProduct.setImageName(imageFile.getOriginalFilename());
                existingProduct.setImageData(imageFile.getBytes());
            }

            catch (IOException e) {
                throw new RuntimeException("Erro ao atualizar imagem");
            }
        }
        productRepository.save(existingProduct);
    }

    @Override
    public void deleteById(Long id) {
        Product product = findById(id);

        productRepository.delete(product);
    }
}
