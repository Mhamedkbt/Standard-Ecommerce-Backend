package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.CloudinaryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- GET ALL ----------
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ---------- GET ONE ----------
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // ---------- CREATE ----------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("previousPrice") double previousPrice,
            @RequestParam("isAvailable") String isAvailableStr,
            @RequestParam("onPromotion") String onPromotionStr,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "existingImages", required = false) String existingImagesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images // Changed to RequestParam
    ) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setPreviousPrice(previousPrice);
            product.setAvailable(Boolean.parseBoolean(isAvailableStr));
            product.setOnPromotion(Boolean.parseBoolean(onPromotionStr));
            product.setCategory(category);
            product.setDescription(description);

            List<String> imageList = new ArrayList<>();

            if (existingImagesJson != null && !existingImagesJson.isBlank()) {
                imageList.addAll(
                        objectMapper.readValue(existingImagesJson, new TypeReference<List<String>>() {})
                );
            }

            if (images != null && !images.isEmpty()) {
                // PRO FIX: Parallel upload to avoid timeout
                List<String> uploadedUrls = images.parallelStream().map(image -> {
                    try {
                        return cloudinaryService.uploadImage(image, "products");
                    } catch (Exception e) {
                        throw new RuntimeException("Cloudinary Error: " + e.getMessage());
                    }
                }).collect(Collectors.toList());
                imageList.addAll(uploadedUrls);
            }

            product.setImages(imageList);
            return productRepository.save(product);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create product: " + e.getMessage());
        }
    }

    // ---------- UPDATE ----------
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("previousPrice") double previousPrice,
            @RequestParam("isAvailable") String isAvailableStr,
            @RequestParam("onPromotion") String onPromotionStr,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "existingImages", required = false) String existingImagesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages // Changed to RequestParam
    ) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setName(name);
            product.setPrice(price);
            product.setPreviousPrice(previousPrice);
            product.setAvailable(Boolean.parseBoolean(isAvailableStr));
            product.setOnPromotion(Boolean.parseBoolean(onPromotionStr));
            product.setCategory(category);
            product.setDescription(description);

            List<String> images = new ArrayList<>();

            if (existingImagesJson != null && !existingImagesJson.isBlank()) {
                images.addAll(
                        objectMapper.readValue(existingImagesJson, new TypeReference<List<String>>() {})
                );
            }

            if (newImages != null && !newImages.isEmpty()) {
                // PRO FIX: Parallel upload
                List<String> uploadedUrls = newImages.parallelStream().map(image -> {
                    try {
                        return cloudinaryService.uploadImage(image, "products");
                    } catch (Exception e) {
                        throw new RuntimeException("Cloudinary Error: " + e.getMessage());
                    }
                }).collect(Collectors.toList());
                images.addAll(uploadedUrls);
            }

            product.setImages(images);
            return productRepository.save(product);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update product: " + e.getMessage());
        }
    }

    // ---------- DELETE ----------
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}