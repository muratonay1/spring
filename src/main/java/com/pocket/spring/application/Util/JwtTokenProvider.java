package com.pocket.spring.application.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pocket.spring.application.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtTokenProvider {


    private final Key accessKey;
    private final Key refreshKey;
    private final ObjectMapper objectMapper;

    @Value("${jwt.accessToken.validity}")
    private long accessTokenValidity;

    @Value("${jwt.refreshToken.validity}")
    private long refreshTokenValidity;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String accessSecret, @Value("${jwt.refresh_secret}") String refreshSecret) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, accessKey);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration, accessKey);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key secretKey) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, Key secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, refreshKey);
    }
    @SuppressWarnings("unchecked")
    public String getSessionIdFromAccessToken(String token){
        Claims claims=extractAllClaims(token,accessKey);
        Map<String, Object> userContext = (Map<String, Object>) claims.get("userContext");
        return (String) userContext.get("sessionId");
    }
    public Date extractExpirationFromRefreshToken(String refreshToken) {
        return extractClaim(refreshToken, Claims::getExpiration, refreshKey);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setClaims(claims(user))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(accessKey)
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setClaims(claims(user))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(refreshKey)
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String token, String username) {
        final String extractedUsername = extractUsernameFromRefreshToken(token);
        return (extractedUsername.equals(username) && !isTokenExpiredFromRefreshToken(token));
    }

    private Boolean isTokenExpiredFromRefreshToken(String token) {
        return extractExpirationFromRefreshToken(token).before(new Date());
    }

    public boolean isTokenValid(String token, Key secretKey) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException e) {
            return false;
        }
    }

    public Map<String, Object> claims(User user) {
        Map<String, Object> userContext = new HashMap<>();
        userContext.put("email", user.getEmail());
        userContext.put("username", user.getUsername());
        userContext.put("name", user.getName());
        userContext.put("surname", user.getSurname());
        userContext.put("nameTitle", user.getNameTitle());
        userContext.put("status", user.getStatus());
        userContext.put("createDate", user.getCreateDate());
        userContext.put("createTime", user.getCreateTime());
        userContext.put("avatar", user.getAvatar());

        List<Map<String, String>> menus = new ArrayList<>();
        menus.add(createMenu("about", "Hakkımızda", "/about"));
        menus.add(createMenu("home", "Home", "/home"));
        menus.add(createMenu("projects", "Projeler", "/projects"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userContext", userContext);
        claims.put("sub", user.getUsername());
        claims.put("menus", menus);

        return claims;
    }

    private Map<String, String> createMenu(String id, String name, String url) {
        Map<String, String> menu = new HashMap<>();
        menu.put("id", id);
        menu.put("name", name);
        menu.put("url", url);
        return menu;
    }
}
