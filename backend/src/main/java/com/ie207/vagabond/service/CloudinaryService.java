package com.ie207.vagabond.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Transactional
    public List<String> uploadImages(List<String> base64Images) throws IOException {
        try {
            List<String> uploadedIds = new ArrayList<>();
            for (String image : base64Images) {
                Map uploadResult = cloudinary.uploader().upload(image, ObjectUtils.asMap(
                                "upload_preset", "ml_default",
                                "width", 867,
                                "height", 578,
                                "crop", "fill",
                                "gravity", "auto"
                        )
                );
                uploadedIds.add((String) uploadResult.get("public_id"));
            }
            return uploadedIds;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public Map deleteImage(String publicId) throws IOException {
        try {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
