package com.asmetsalud.nexus.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;

@Configuration

public class JwtConfig {
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        } else {
            throw new IllegalArgumentException("Encabezado Authorization no contiene un token Bearer");
        }
    }

    // Método para decodificar un JWT
    public Claims parseJwt(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor("c0225b38-f347-488f-97e7-661e6ffbcc6d".getBytes())) // Clave secreta
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
