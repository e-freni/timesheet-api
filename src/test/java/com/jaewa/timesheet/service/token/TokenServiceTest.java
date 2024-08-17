package com.jaewa.timesheet.service.token;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final Long validityMillis = 3600000L; // 1 ora
    private final String secret = "\n" +
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MjM5MTc4MDAsImV4cCI6MTc1NTQ1MzgwMCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJFbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWluaXN0cmF0b3IiXX0.Haf5E9fXuQ4Tg9bQGWseLbdOHmW20EXpPdY3F3z-cBv9fgjdK-CG3qXr63-IeowEO6rlhQjzX5fVwneNC-GFsw"; // Deve essere abbastanza lungo per HS512
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(validityMillis, secret);
    }

    @Test
    void createTokenShouldReturnValidToken() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.USER);

        String token = tokenService.createToken(user);
        assertNotNull(token);

        // Decodifica il token per verificare i contenuti
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"));
        assertEquals("1", claims.get("userId").toString());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateTokenAndGetAuthenticationShouldReturnAuthenticationForValidToken() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.USER);

        String token = tokenService.createToken(user);

        Authentication authentication = tokenService.validateTokenAndGetAuthentication(token);

        assertNotNull(authentication);
        assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
        assertEquals("testuser", authentication.getName());

        Token tokenPrincipal = (Token) authentication.getPrincipal();
        assertEquals("testuser", tokenPrincipal.getName());
        assertEquals(1L, tokenPrincipal.getApplicationUserId());

        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void validateTokenAndGetAuthenticationShouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThrows(io.jsonwebtoken.JwtException.class, () -> {
            tokenService.validateTokenAndGetAuthentication(invalidToken);
        });
    }
}
