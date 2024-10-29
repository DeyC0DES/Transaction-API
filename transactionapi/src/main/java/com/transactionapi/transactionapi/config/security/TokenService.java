package com.transactionapi.transactionapi.config.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.transactionapi.transactionapi.models.AccountModel;

@Service
public class TokenService {
    
    @Value("${security.secret.token}")
    private String secret;

    public String generateToken(AccountModel model) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("transaction-api-security")
                    .withSubject(model.getEmail())
                    .withExpiresAt(generateExpireAt())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error while auhtentication: GENERATE: " + e.getMessage(), e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                .withIssuer("transaction-api-security")
                .build()
                .verify(token)
                .getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private Instant generateExpireAt() {
        return LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.UTC);
    }
}
