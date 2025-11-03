package com.ie207.vagabond.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {
    SecretKey key = Keys.hmacShaKeyFor(JwtContant.SECRET_KEY.getBytes());

    public void generateToken(Authentication auth, HttpServletResponse response) {
        String id = (String) auth.getPrincipal();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                //.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 ngày
                .claim("id", id)
                .claim("authority", role)
                .signWith(key)
                .compact();

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        Set<String> auths=new HashSet<>();
        for(GrantedAuthority authority:collection) {
            auths.add(authority.getAuthority());
        }
        return String.join(",",auths);
    }
}
