package com.adega.adega.controller.Client;

import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/store")
public class StoreController {

    private final ProductRepository productRepository;

    public StoreController (ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/", "/homepage"})
    public String home(Model model) {
        model.addAttribute("product", productRepository.findByActiveTrue());
        return "client/homepage";
    }

    @GetMapping("/products/image/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImageData());
    }
}
