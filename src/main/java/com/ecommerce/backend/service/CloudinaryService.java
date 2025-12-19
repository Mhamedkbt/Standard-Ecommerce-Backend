package com.ecommerce.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Using getInputStream() instead of getBytes() to save RAM
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getInputStream(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "auto" // Detects if it's jpg, png, etc.
                )
        );
        return uploadResult.get("secure_url").toString();
    }
}