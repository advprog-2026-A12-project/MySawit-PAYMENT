package id.ac.ui.cs.advprog.mysawitpayment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret;
    private SecretKey key;
    private String token;

    @BeforeEach
    void setUp() {
        secret = "c3VwZXJzZWNyZXRzdXBlcnNlY3JldHN1cGVyc2VjcmV0";

        jwtUtil = new JwtUtil(secret);

        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        token = Jwts.builder()
                .subject("user-123")
                .claim("role", "ADMIN")
                .claim("email", "admin@test.com")
                .issuedAt(new Date())
                .signWith(key)
                .compact();
    }

    @Test
    void testExtractClaims() {
        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("user-123", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("admin@test.com", claims.get("email"));
    }

    @Test
    void testGetUserId() {
        String userId = jwtUtil.getUserId(token);

        assertEquals("user-123", userId);
    }

    @Test
    void testGetRole() {
        String role = jwtUtil.getRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void testGetEmail() {
        String email = jwtUtil.getEmail(token);

        assertEquals("admin@test.com", email);
    }

    @Test
    void testIsValidTrue() {
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void testIsValidFalse() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtUtil.isValid(invalidToken));
    }
}