package com.adega.adega.controller.Admin;

import com.adega.adega.entity.Product;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.adega.adega.util.ProductCategories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;

    private final ProductService productService;

    public ProductController (ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;

    }


    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        return "admin/new_product";
    }


    @GetMapping
    public String listProducts (@RequestParam(value = "keyword", required = false) String keyword,
                                Model model) {

        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(keyword);
        }
        else {
            products = productRepository.findAll();
        }
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);

        return "admin/products";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);

        if(product == null ||product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(product.getImageData());
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);

        model.addAttribute("product", product);
        return "admin/edit_product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (!ProductCategories.isValid(product.getCategory())) {
            result.rejectValue("category","category.invalid",
                    "Selecione uma categoria válida.");
        }

        if (result.hasErrors()) {
            product.setId(id);

            Product existingProduct = productService.findById(id);
            product.setImageData(existingProduct.getImageData());
            product.setStock(existingProduct.getStock());
            return "admin/edit_product";
        }

        productService.updateProduct(id, product, imageFile);

        redirectAttributes.addFlashAttribute("success", "Produto atualizado com sucesso!");

        return "redirect:/admin/products";
    }

    @PostMapping("/save")
    public String saveProduct (@Valid @ModelAttribute ("product") Product product,
        BindingResult result,
    @RequestParam ("imageFile") MultipartFile imageFile,
                               Model model) {

        if (!ProductCategories.isValid(product.getCategory())) {
            result.rejectValue("category", "category.invalid",
                    "Selecione uma categoria válida");
        }

      if (result.hasErrors()) {
          return "admin/new_product";
      }

      try {
          if(!imageFile.isEmpty()) {
              String uploadDir = "uploads/products/";

              Files.createDirectories(Paths.get(uploadDir));

              String fileName = imageFile.getOriginalFilename();

              Path filePath = Paths.get(uploadDir, fileName);

              Files.copy(
                      imageFile.getInputStream(),
                      filePath,
                      StandardCopyOption.REPLACE_EXISTING
              );

              product.setImageName(fileName);
              product.setImagePath(filePath.toString());
              product.setImageData(imageFile.getBytes());
          }

          product.setActive(true);
          product.setStock(0);

          productRepository.save(product);
          return "redirect:/admin/products";

      } catch (IOException e) {
          model.addAttribute("errorMessage", "Erro ao salvar imagem do produto");
          return "admin/new_product";
      }

    }

        @PostMapping("/delete/{id}")
        public String deleteProduct(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        productService.deleteById(id);

        redirectAttributes.addFlashAttribute("success", "Produto excluído com sucesso!");

        return "redirect:/admin/products";
        }

        @ModelAttribute("categories")
        public List<String> categories() {
        return ProductCategories.ALL;
        }
}
