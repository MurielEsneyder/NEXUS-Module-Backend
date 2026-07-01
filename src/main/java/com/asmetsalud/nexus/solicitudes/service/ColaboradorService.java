package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.config.JwtConfig;
import com.asmetsalud.nexus.solicitudes.dto.ColaboradorDTO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColaboradorService {

    private final JwtConfig jwtConfig;

    public ColaboradorDTO obtenerColaboradorActual() {
        log.info("🔍 Obteniendo datos del colaborador");

        try {
            String username = null;

            // ============================================================
            // OBTENER TOKEN DEL REQUEST
            // ============================================================
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    log.info("🔑 Token recibido");

                    try {
                        Claims claims = jwtConfig.parseJwt(token);
                        username = claims.getSubject();
                        log.info("👤 Username extraído del token: {}", username);
                    } catch (Exception e) {
                        log.error("❌ Error al parsear token: {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ No hay token en la petición");
                }
            }

            // ============================================================
            // FALLBACK: Usar "julian.calambas"
            // ============================================================
            if (username == null || username.isEmpty()) {
                log.warn("⚠️ Usando fallback: julian.calambas");
                username = "julian.calambas";
            }

            // ============================================================
            // CREAR DTO
            // ============================================================
            ColaboradorDTO dto = new ColaboradorDTO();
            dto.setNombreCompleto(username);
            dto.setEmail(username + "@asmetsalud.com");
            dto.setCargo("Colaborador");
            dto.setSede("Sede Principal");
            dto.setDocumento(null);

            log.info("✅ Datos devueltos: {}", dto);
            return dto;

        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage());
            return crearFallback();
        }
    }

    private ColaboradorDTO crearFallback() {
        ColaboradorDTO dto = new ColaboradorDTO();
        dto.setNombreCompleto("julian.calambas");
        dto.setEmail("julian.calambas@asmetsalud.com");
        dto.setCargo("Colaborador");
        dto.setSede("Sede Principal");
        dto.setDocumento(null);
        return dto;
    }
}