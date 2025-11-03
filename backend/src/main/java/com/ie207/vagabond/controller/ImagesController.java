package com.ie207.vagabond.controller;

import com.ie207.vagabond.service.CloudinaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/img")
@RequiredArgsConstructor
public class ImagesController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/update")
    public ResponseEntity<?> uploadImages(
            @RequestBody Map<String, Object> body
    ) {
        try {
            Object data = body.get("data");
            if (!(data instanceof List<?> files)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Data must be an array of images"));
            }

            List<String> base64Images = files.stream()
                    .map(Object::toString)
                    .toList();

            List<String> publicIds = cloudinaryService.uploadImages(base64Images);
            return ResponseEntity.ok(publicIds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Delete failed"));
        }
    }

    @DeleteMapping("/delete/**")
    public ResponseEntity<?> deleteImage(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String publicId = requestURI.substring(requestURI.indexOf("/delete/") + 8);
            if (publicId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Public ID is required"));
            }
            Map result = cloudinaryService.deleteImage(publicId);
            if (!"ok".equals(result.get("result"))) {
                return ResponseEntity.status(404).body(Map.of("error", "Image not found or already deleted"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Image deleted successfully",
                    "result", result
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Delete failed"));
        }
    }
}
