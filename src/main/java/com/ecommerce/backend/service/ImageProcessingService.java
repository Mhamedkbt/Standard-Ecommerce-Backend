package com.ecommerce.backend.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value; // 🎯 NEW: Import Value for config injection
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger; // 🎯 CRITICAL: Import Logger
import org.slf4j.LoggerFactory; // 🎯 CRITICAL: Import LoggerFactory

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageProcessingService {

    // --- CRITICAL: Logger Instance ---
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    // --- CRITICAL: Use injected value for UPLOAD_DIR (Recommended for Production) ---
    @Value("${file.upload-dir:./uploads/}") // Use application.properties or application.yml
    private String configuredUploadDir;

    // --- Helper to ensure the directory exists ---
    private void ensureUploadDir() {
        // Use the configured path
        File uploadDir = new File(configuredUploadDir);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                logger.info("Created upload directory: {}", configuredUploadDir);
            } else {
                logger.error("Failed to create upload directory: {}", configuredUploadDir);
                // In a real app, you might throw a RuntimeException here to halt startup
            }
        }
    }

    /**
     * Executes the initial file save (fast) and immediately delegates the slow optimization
     * to a background thread. Returns the file path instantly.
     * @param targetWidth The desired max width for the optimized image.
     */
    public String saveAndDelegateOptimization(MultipartFile file, int targetWidth) throws IOException {

        ensureUploadDir();
        if (file == null || file.isEmpty()) {
            logger.warn("Attempted to save a null or empty file.");
            return null;
        }

        String originalFilename = file.getOriginalFilename();

        // Sanitize the filename
        String sanitizedOriginal = originalFilename != null ? originalFilename
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_.-]", "")
                : "untitled_file";

        // 1. Generate the final file path (e.g., /uploads/unique_name.jpg)
        String filename = UUID.randomUUID() + "_" + sanitizedOriginal;
        File finalImageFile = new File(configuredUploadDir, filename);

        // 2. Save the RAW, unoptimized file content to the final path (FAST OPERATION).
        // This is necessary so the background thread has a file to read/overwrite.
        logger.info("Saving raw file to: {}", finalImageFile.getAbsolutePath());
        file.transferTo(finalImageFile);

        // 3. Kick off the slow resizing process in the background.
        optimizeImageInSeparateThread(finalImageFile, targetWidth);

        // 4. Return the public URL path INSTANTLY.
        // NOTE: The relative URL must match what your web server is configured to serve static content from.
        return "/uploads/" + filename;
    }

    /**
     * The slow, CPU-intensive image resizing operation.
     * Runs in a separate thread because of @Async.
     */
    @Async
    public void optimizeImageInSeparateThread(File imageFile, int targetWidth) {
        // Log start of slow operation
        logger.info("Starting async optimization for: {} to width {}", imageFile.getName(), targetWidth);

        try {
            // Check existence before processing
            if (imageFile.exists()) {
                // Overwrite the file at the same path with the optimized version
                Thumbnails.of(imageFile)
                        .width(targetWidth)
                        .outputQuality(0.8) // 80% quality for smaller size
                        .toFile(imageFile); // Overwrites the file!

                // Log success
                logger.info("Successfully optimized image: {}", imageFile.getName());
            } else {
                // Log a missing file error
                logger.warn("Cannot optimize image. File not found at path: {}", imageFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // Log the exception in detail
            logger.error("Async Error optimizing image: {}", imageFile.getName(), e);
        }
    }
}