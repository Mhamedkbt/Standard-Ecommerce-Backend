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
        Product product = new Product();
        return processAndSave(product, name, price, previousPrice, isAvailableStr, onPromotionStr, category, description, existingImagesJson, images);
    }

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
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return processAndSave(product, name, price, previousPrice, isAvailableStr, onPromotionStr, category, description, existingImagesJson, newImages);
    }

    @Transactional // Added: Ensures data integrity
    protected Product processAndSave(Product product, String name, Double price, double prevPrice, String isAvail, String onPromo, String cat, String desc, String existJson, List<MultipartFile> files) {
        try {
            List<String> finalImageList = new ArrayList<>();

            if (existJson != null && !existJson.isBlank() && !existJson.equals("null") && !existJson.equals("[]")) {
                List<String> existing = objectMapper.readValue(existJson, new TypeReference<List<String>>() {});
                finalImageList.addAll(existing);
            }

            if (files != null && !files.isEmpty()) {
                List<String> newUrls = files.parallelStream()
                        .filter(file -> file != null && !file.isEmpty())
                        .map(file -> {
                            try {
                                return cloudinaryService.uploadImage(file, "products");
                            } catch (Exception e) {
                                throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
                            }
                        }).collect(Collectors.toList());
                finalImageList.addAll(newUrls);
            }

            product.setName(name);
            product.setPrice(price);
            product.setPreviousPrice(prevPrice);
            product.setAvailable(Boolean.parseBoolean(isAvail));
            product.setOnPromotion(Boolean.parseBoolean(onPromo));
            product.setCategory(cat);
            product.setDescription(desc);
            product.setImages(finalImageList);

            return productRepository.save(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Server Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}