package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.User;
import com.ie207.vagabond.request.LogInRequest;
import com.ie207.vagabond.request.RegisterRequest;
import com.ie207.vagabond.request.ResetPasswordRequest;
import com.ie207.vagabond.response.ApiResponse;
import com.ie207.vagabond.response.UserResponse;
import com.ie207.vagabond.service.AuthService;
import com.ie207.vagabond.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(HttpServletResponse response,
                                               @RequestBody RegisterRequest req) throws Exception {
        String message = authService.sendOtp(req.getEmail(), response);
        return new ResponseEntity<>(new ApiResponse(message, true), HttpStatus.CREATED);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestBody RegisterRequest req) throws Exception {
        try {
            String message = authService.register(req, request, response);
            return new ResponseEntity<>(new ApiResponse(message, true), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Lỗi hệ thống: " + e.getMessage(), false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LogInRequest req,
                                             HttpServletResponse response) throws Exception {
        try {
            String message = authService.login(req, response);
            return ResponseEntity.ok(new ApiResponse(message, true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Lỗi hệ thống: " + e.getMessage(), false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        String message = authService.logOut(response);
        return ResponseEntity.ok(new ApiResponse(message, true));
    }

    @GetMapping("/")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) throws Exception {
        UserResponse user = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) throws MessagingException {
        authService.resetPassword(email);
        return ResponseEntity.ok(
                Map.of("message", "Reset password link đã được gửi về email")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) throws RuntimeException {
        authService.confirmResetPassword(
                request.getToken(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(
                Map.of("message", "Đổi mật khẩu thành công")
        );
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> requestBody, HttpServletResponse response) {
        String authCode = requestBody.get("code");
        if (authCode == null || authCode.isBlank()) {
            return ResponseEntity.badRequest().body("Auth code is required");
        }
        try {
            String message = authService.loginWithGoogle(authCode, response);
            return ResponseEntity.ok(
                    Map.of("message", message)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

}
