package com.ecoride.user_service.config;

import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// User-Service ke config package mein
@Component
public class JwtUtil {
    // 1. Sabse pehle apni Secret Key fix karo (Minimum 32 characters)
    private String SECRET_KEY = "Mera_Secret_Indore_EcoRide_Project_2026_Unique";

    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey()) // Yahan humne Key use ki
                .compact();
    }
    }

