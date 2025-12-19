package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.CloudinaryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Product createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("previousPrice") double previousPrice,
            @RequestParam("isAvailable") String isAvailableStr,
            @RequestParam("onPromotion") String onPromotionStr,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "existingImages", required = false) String existingImagesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            Product product = new Product();
            return saveProductData(product, name, price, previousPrice, isAvailableStr, onPromotionStr, category, description, existingImagesJson, images);
        } catch (Exception e) {
            throw new RuntimeException("Create Failed: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
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
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages
    ) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            return saveProductData(product, name, price, previousPrice, isAvailableStr, onPromotionStr, category, description, existingImagesJson, newImages);
        } catch (Exception e) {
            throw new RuntimeException("Update Failed: " + e.getMessage());
        }
    }

    private Product saveProductData(Product product, String name, Double price, double prevPrice, String isAvail, String onPromo, String cat, String desc, String existJson, List<MultipartFile> files) throws Exception {

        List<String> finalImageList = new ArrayList<>();

        // 1. Handle Existing Images (Preserve order)
        if (existJson != null && !existJson.isBlank() && !existJson.equals("[]") && !existJson.equals("null")) {
            List<String> existing = objectMapper.readValue(existJson, new TypeReference<List<String>>() {});
            finalImageList.addAll(existing);
        }

        // 2. Handle New Uploads (FIX: Parallel Upload to prevent 500 Timeouts)
        if (files != null && !files.isEmpty()) {
            List<String> uploadedUrls = files.parallelStream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            return cloudinaryService.uploadImage(file, "products");
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            finalImageList.addAll(uploadedUrls);
        }

        product.setName(name);
        product.setPrice(price);
        product.setPreviousPrice(prevPrice);
        product.setAvailable(Boolean.parseBoolean(isAvail));
        product.setOnPromotion(Boolean.parseBoolean(onPromo));
        product.setCategory(cat);
        product.setDescription(desc);

        // Update image list
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        } else {
            product.getImages().clear();
        }
        product.getImages().addAll(finalImageList);

        return productRepository.saveAndFlush(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}