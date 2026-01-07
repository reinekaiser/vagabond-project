package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import com.ie207.vagabond.repository.UserRepository;
import com.ie207.vagabond.response.UserResponse;
import com.ie207.vagabond.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return ResponseEntity.ok(userService.getAllUsers(Role.USER, pageable));
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(
            Authentication authentication,
            @RequestBody User user
    ) {
        String userId = (String) authentication.getPrincipal();
        UserResponse updatedUser = userService.updateUser(userId, user);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> passwords
    ) {
        try {
            String oldPassword = passwords.get("oldPassword");
            String newPassword = passwords.get("newPassword");
            String userId = (String) authentication.getPrincipal();
            UserResponse updatedUser = userService.changePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Đổi mật khẩu thành công",
                    "user", updatedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Đổi mật khẩu thất bại",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/update-avatar")
    public ResponseEntity<UserResponse> updateAvatar(
            Authentication authentication,
            @RequestBody Map<String, String> avatars
    ) throws IOException {
        String userId = (String) authentication.getPrincipal();

        String avatarBase64 = avatars.get("avatar");
        return ResponseEntity.ok(userService.updateUserAvatar(userId, avatarBase64));
    }

    @DeleteMapping("/delete-avatar")
    public ResponseEntity<UserResponse> deleteAvatar(
            Authentication authentication
    ) throws IOException {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(
                userService.deleteUserAvatar(userId)
        );
    }
}
