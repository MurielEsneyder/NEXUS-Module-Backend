package com.asmetsalud.nexus.config;
import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("🚀 [API Request] {} {}", request.getMethod(), request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        log.info("🔑 Header Authorization: {}", authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ No hay token en la petición");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final Claims claims = jwtConfig.parseJwt(jwt);
            final String username = claims.getSubject();
            log.info("👤 Username extraído del token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                authToken.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("✅ Usuario autenticado correctamente: {}", username);
            }

        } catch (Exception e) {
            log.error("❌ Error al autenticar usuario: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}