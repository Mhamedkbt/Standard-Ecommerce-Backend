package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // ✅ GET ALL PRODUCTS
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ CREATE PRODUCT (JSON ONLY)
    @PostMapping
    @Transactional
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // ✅ UPDATE PRODUCT (JSON ONLY)
    @PutMapping("/{id}")
    @Transactional
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updated
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(updated.getName());
        product.setPrice(updated.getPrice());
        product.setPreviousPrice(updated.getPreviousPrice());
        product.setAvailable(updated.isAvailable());
        product.setOnPromotion(updated.isOnPromotion());
        product.setCategory(updated.getCategory());
        product.setDescription(updated.getDescription());
        product.setImages(updated.getImages());

        return productRepository.save(product);
    }

    // ✅ DELETE PRODUCT
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}
