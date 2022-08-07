package com.jaewa.timesheet.service.token;

import com.jaewa.timesheet.model.ApplicationUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;


@Service
public class TokenService {

    private static final String ROLE_KEY = "role";
    private static final String USER_ID = "userId";
    private final Key key;
    private final Long validityMillis;

    public TokenService(@Value("${jwt.validityMillis}") Long validityMillis, @Value("${jwt.secret}") String secret) {
        this.validityMillis = validityMillis;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(ApplicationUser user) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + validityMillis);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(ROLE_KEY, user.getRole().getAuthority())
                .claim(USER_ID, user.getId())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public Authentication validateTokenAndGetAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(claims.get(ROLE_KEY).toString());
        //FIXME get a better way to convert di userId
        Token t = new Token(claims.getSubject(), Long.valueOf((Integer) claims.get(USER_ID)));
        return new UsernamePasswordAuthenticationToken(t, token, List.of(authority));
    }

}

