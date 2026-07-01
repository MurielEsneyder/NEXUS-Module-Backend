package com.asmetsalud.nexus.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Configuration
public class JwtConfig {

    @Value("c0225b38-f347-488f-97e7-661e6ffbcc6d")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * Obtiene la clave secreta para firmar los JWT
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el token del encabezado Authorization
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Encabezado Authorization no contiene un token Bearer");
    }

    /**
     * Decodifica un JWT y devuelve los Claims
     */
    public Claims parseJwt(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    /**
     * Verifica si un token ha expirado
     */
    public boolean isTokenExpired(String jwt) {
        Claims claims = parseJwt(jwt);
        Date expirationDate = claims.getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * Obtiene el nombre de usuario del token
     */
    public String getUsernameFromToken(String jwt) {
        Claims claims = parseJwt(jwt);
        return claims.getSubject();
    }

    /**
     * Obtiene los roles del token
     */
    public String getRolesFromToken(String jwt) {
        Claims claims = parseJwt(jwt);
        return claims.get("roles", String.class);
    }

    // ============================================================
    // NUEVOS MÉTODOS PARA EXTRAER CARGO, SEDE Y EMAIL
    // ============================================================

    /**
     * Obtiene el cargo del token
     */
    public String getCargoFromToken(String jwt) {
        Claims claims = parseJwt(jwt);
        return claims.get("cargo", String.class);
    }

    /**
     * Obtiene la sede del token
     */
    public String getSedeFromToken(String jwt) {
        Claims claims = parseJwt(jwt);
        return claims.get("sede", String.class);
    }

    /**
     * Obtiene el email del token
     */
    public String getEmailFromToken(String jwt) {
        Claims claims = parseJwt(jwt);
        return claims.get("email", String.class);
    }

    public long getExpiration() {
        return expiration;
    }
}