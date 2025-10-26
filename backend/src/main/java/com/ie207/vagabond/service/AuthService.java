package com.ie207.vagabond.service;

import com.ie207.vagabond.config.JwtProvider;
import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import com.ie207.vagabond.repository.UserRepository;
import com.ie207.vagabond.request.LogInRequest;
import com.ie207.vagabond.request.RegisterRequest;
import com.ie207.vagabond.utils.OtpUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private static final String OTP_SECRET = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGH123456";
    private static final long OTP_EXPIRATION = 3 * 60 * 1000;
    private final SecretKey key = Keys.hmacShaKeyFor(OTP_SECRET.getBytes());

    public String sendOtp(String email, HttpServletResponse response) throws MessagingException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new MessagingException("Email đã được sử dụng!");
        }
        String otp = OtpUtils.generateOtp();
        String otpToken = generateOtpToken(email, otp);
        Cookie cookie = new Cookie("otp_token", otpToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (OTP_EXPIRATION / 1000));
        response.addCookie(cookie);
        emailService.sendVerificationOtpEmail(
                email,
                otp,
                "Account Verification",
                "Your verification OTP is: " + otp + "\nThis OTP will expire in 3 minutes."
        );
        return "✅ OTP đã được gửi tới email.";
    }

    public String register(RegisterRequest request,
                           HttpServletRequest httpRequest,
                           HttpServletResponse response) {
        try {
            Claims claims = extractClaimsFromCookie(httpRequest, "otp_token");
            if (claims == null) return "OTP token không tồn tại hoặc đã hết hạn!";
            String email = claims.getSubject();
            String otp = claims.get("otp", String.class);
            if (!otp.equals(request.getOtp())) {
                throw new IllegalArgumentException("OTP không đúng hoặc đã hết hạn!");
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setGender("Unknown");
            user.setRole(Role.USER);
            userRepository.save(user);

            clearCookie(response, "otp_token");

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            jwtProvider.generateToken(authentication, response);

            return "Đăng ký thành công!";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    private String generateOtpToken(String email, String otp) {
        return Jwts.builder()
                .setSubject(email)
                .claim("otp", otp)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + OTP_EXPIRATION))
                .signWith(key)
                .compact();
    }

    private Claims extractClaimsFromCookie(HttpServletRequest request, String cookieName) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie == null) return null;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cookie.getValue())
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie clearCookie = new Cookie(name, null);
        clearCookie.setMaxAge(0);
        clearCookie.setPath("/");
        response.addCookie(clearCookie);
    }

    public String login (LogInRequest req, HttpServletResponse response) {
        Optional<User> user = userRepository.findByEmail(req.getEmail());
        if (user.isEmpty() || !passwordEncoder.matches(req.getPassword(), user.get().getPassword())) {
            throw new IllegalArgumentException("Thông tin đăng nhập không đúng!");
        }
        User currentUser = user.get();
        List<GrantedAuthority> authority = new ArrayList<>();
        authority.add(new SimpleGrantedAuthority(currentUser.getRole().toString()));
        String userId = String.valueOf(currentUser.getId());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authority);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        jwtProvider.generateToken(authentication, response);

        return "Đăng nhập thành công!";
    }

    public String logOut(HttpServletResponse response) {
        clearCookie(response, "jwt");
        SecurityContextHolder.clearContext();


        return "Đăng xuất thành công!";
    }
}
