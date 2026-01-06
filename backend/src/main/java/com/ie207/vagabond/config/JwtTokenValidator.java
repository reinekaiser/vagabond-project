package com.ie207.vagabond.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JwtTokenValidator extends OncePerRequestFilter {
    private static final Map<String, List<String>> PUBLIC_ENDPOINTS = Map.of(
            "POST", List.of(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/send-otp"
            ),
            "GET", List.of(
                    "/api/hotel/**",
                    "/api/tours/**",
                    "/api/cities/**"
            )
    );

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        Cookie jwtCookie = WebUtils.getCookie(request, "jwt");
        String jwt = null;
        if (jwtCookie != null) {
            jwt = jwtCookie.getValue();
            System.out.println("JWT từ Cookie: " + jwt);
        }
        if (jwt != null) {
            try {
                SecretKey key = Keys.hmacShaKeyFor(JwtContant.SECRET_KEY.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();
                System.out.println("Decode Cookie... " + key);
                String userId = String.valueOf(claims.get("id"));
                String authority = String.valueOf(claims.get("authority"));
                System.out.println("authority -------- " + authority + "id -----" + userId);

                List<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority(authority));
                Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, auth);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid JWT token..." + e);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();

        List<String> publicPatterns = PUBLIC_ENDPOINTS.get(method);
        if (publicPatterns == null) return false;

        return publicPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}