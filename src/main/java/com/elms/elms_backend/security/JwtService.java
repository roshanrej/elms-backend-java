package com.elms.elms_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for:
 * - JWT token generation
 * - token validation
 * - claim extraction
 * - access and refresh token workflows
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generates HMAC signing key from
     * configured JWT secret.
     *
     * @return JWT signing key
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes()
        );
    }

    /**
     * Generates short-lived access token
     * used for API authentication.
     *
     * Access token expiry:
     * 15 minutes
     *
     * @param userDetails authenticated user
     * @return signed JWT access token
     */
    public String generateAccessToken(
            UserDetails userDetails
    ) {

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 15
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Generates long-lived refresh token
     * used to obtain new access tokens.
     *
     * Refresh token expiry:
     * 7 days
     *
     * @param userDetails authenticated user
     * @return signed JWT refresh token
     */
    public String generateRefreshToken(
            UserDetails userDetails
    ) {

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000L * 60 * 60 * 24 * 7
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Extracts username/email subject
     * from JWT token payload.
     *
     * @param token JWT token
     * @return token subject
     */
    public String extractUsername(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    /**
     * Extracts specific claim from JWT payload.
     *
     * @param token JWT token
     * @param claimsResolver claim extraction function
     * @param <T> claim type
     * @return extracted claim value
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Parses and validates JWT payload
     * using configured signing key.
     *
     * @param token JWT token
     * @return parsed JWT claims
     */
    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates token ownership and expiry state.
     *
     * @param token JWT token
     * @param userDetails authenticated user
     * @return true if token is valid
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username =
                extractUsername(token);

        return username.equals(
                userDetails.getUsername()
        ) && !isTokenExpired(token);
    }

    /**
     * Checks whether JWT token
     * has expired.
     *
     * @param token JWT token
     * @return true if token expired
     */
    private boolean isTokenExpired(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }
}