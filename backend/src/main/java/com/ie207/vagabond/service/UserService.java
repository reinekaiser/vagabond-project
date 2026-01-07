package com.ie207.vagabond.service;

import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import com.ie207.vagabond.repository.UserRepository;
import com.ie207.vagabond.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public UserResponse getCurrentUser(Authentication authentication) throws Exception {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return mapToUserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Role role, Pageable pageable) {
        Page<User> usersPage = userRepository.findAllByRole(role, pageable);
        return usersPage.map(this::mapToUserResponse);
    }

    @Transactional
    public UserResponse updateUserAvatar(String userId, String base64Avatar) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isBlank()) {
            cloudinaryService.deleteImage(user.getAvatarPublicId());
        }
        List<String> uploadedIds = cloudinaryService.uploadImages(List.of(base64Avatar));
        if (uploadedIds == null || uploadedIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload avatar failed"
            );
        }

        String publicId = uploadedIds.get(0);
        String avatarUrl = "https://res.cloudinary.com/dytiq61hf/image/upload/v1767436125/"
                + publicId;
        user.setAvatarPublicId(publicId);
        user.setAvatarUrl(avatarUrl);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse deleteUserAvatar(String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getAvatarPublicId() == null || user.getAvatarPublicId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User has no avatar to delete"
            );
        }

        cloudinaryService.deleteImage(user.getAvatarPublicId());
        user.setAvatarPublicId(null);
        user.setAvatarUrl(null);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateUser(String userId, User user) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            userToUpdate.setFirstName(user.getFirstName());
        }

        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            userToUpdate.setLastName(user.getLastName());
        }

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            userToUpdate.setPhoneNumber(user.getPhoneNumber());
        }

        if (user.getGender() != null && !user.getGender().isBlank()) {
            userToUpdate.setGender(user.getGender());
        }

        if (user.getCity() != null && !user.getCity().isBlank()) {
            userToUpdate.setCity(user.getCity());
        }

        if (user.getNationality() != null && !user.getNationality().isBlank()) {
            userToUpdate.setNationality(user.getNationality());
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            userToUpdate.setAvatarUrl(user.getAvatarUrl());
        }

        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isBlank()) {
            userToUpdate.setAvatarPublicId(user.getAvatarPublicId());
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            userToUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getDateOfBirth() != null) {
            userToUpdate.setDateOfBirth(user.getDateOfBirth());
        }

        User updatedUser = userRepository.save(userToUpdate);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        User usr = userRepository.save(user);
        return mapToUserResponse(usr);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.set_id(user.get_id());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setCity(user.getCity());
        response.setNationality(user.getNationality());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setAvatarPublicId(user.getAvatarPublicId());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}
