package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.ColaboradorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColaboradorService {

    @Qualifier("db2JdbcTemplate")
    private final JdbcTemplate db2JdbcTemplate;

    public ColaboradorDTO obtenerColaboradorActual() {
        log.info("🔍 Obteniendo datos del colaborador desde BD Oracle");

        try {
            // Obtener username del contexto de seguridad
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            log.info("👤 Username obtenido: {}", username);

            // Intentar extraer email desde los claims del token
            String email = username;
            if (auth != null && auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> claims = (Map<String, Object>) auth.getDetails();
                if (claims.containsKey("email")) {
                    email = (String) claims.get("email");
                } else if (claims.containsKey("correo")) {
                    email = (String) claims.get("correo");
                }
            }

            // Extraer prefijo del email para búsqueda con LIKE (ej: jhojan.muriel@%)
            String emailPrefix = email;
            if (email != null && email.contains("@")) {
                emailPrefix = email.substring(0, email.indexOf("@"));
            }

            log.info("🔍 Buscando por email: {}@%", emailPrefix);

            // ============================================================
            // CONSULTAR BASE DE DATOS ORACLE - BUSCANDO POR EMAIL
            // ============================================================
            String sql = """
                SELECT
                    p.ID_PERSONA,
                    TRIM(NVL(p.NOMBRE, '') || ' ' ||
                         NVL(p.PRIMER_APELLIDO, '') || ' ' ||
                         NVL(p.SEGUNDO_APELLIDO, '')) AS NOMBRE_COMPLETO,
                    p.EMAIL AS CORREO,
                    p.NOR_IDENTIFICACION AS IDENTIFICACION,
                    e.COD_USER,
                    c.CARGO_NOMBRE AS CARGO,
                    s.NOMBRE_SEDE AS SEDE
                FROM PAR_PERSONA p
                LEFT JOIN PAR_EMPLEADO_EPS e ON p.ID_PERSONA = e.ID_PERSONA
                LEFT JOIN PAR_CARGO c ON e.ID_CARGO = c.ID_CARGO
                LEFT JOIN PAR_SEDE s ON e.ID_SEDE = s.ID_SEDE
                WHERE p.ESTADO <> 'R'
                  AND p.EMAIL LIKE ? || '@%'
            """;

            Map<String, Object> result = db2JdbcTemplate.queryForMap(sql, emailPrefix);

            // ============================================================
            // CREAR DTO CON DATOS DE LA BD
            // ============================================================
            ColaboradorDTO dto = new ColaboradorDTO();
            dto.setNombreCompleto((String) result.get("NOMBRE_COMPLETO"));
            dto.setEmail((String) result.get("CORREO"));
            dto.setDocumento((String) result.get("IDENTIFICACION"));
            dto.setCargo((String) result.get("CARGO"));
            dto.setSede((String) result.get("SEDE"));
            dto.setIdPersona(result.get("ID_PERSONA") != null ? ((Number) result.get("ID_PERSONA")).longValue() : null);
            dto.setCodUser((String) result.get("COD_USER"));

            log.info("✅ Datos obtenidos de BD: {} - {}", dto.getNombreCompleto(), dto.getEmail());
            return dto;

        } catch (Exception e) {
            log.error("❌ Error consultando BD Oracle: ", e);
            throw new RuntimeException("Error al consultar datos de colaborador en BD Oracle: " + e.getMessage(), e);
        }
    }

    private ColaboradorDTO obtenerDatosDesdeToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return crearFallback();
            }
            String username = auth.getName();

            ColaboradorDTO dto = new ColaboradorDTO();
            if (auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> claims = (Map<String, Object>) auth.getDetails();
                dto.setNombreCompleto((String) claims.getOrDefault("nombreCompleto", username));
                dto.setEmail((String) claims.getOrDefault("email", username + "@asmetsalud.com"));
                dto.setDocumento((String) claims.getOrDefault("documento", ""));
                dto.setCargo((String) claims.getOrDefault("cargo", "Colaborador"));
                dto.setSede((String) claims.getOrDefault("sede", "Sede Principal"));
            } else {
                dto.setNombreCompleto(username);
                dto.setEmail(username + "@asmetsalud.com");
                dto.setCargo("Colaborador");
                dto.setSede("Sede Principal");
                dto.setDocumento("");
            }

            log.info("✅ Usando datos del token: {}", dto);
            return dto;

        } catch (Exception e) {
            log.error("❌ Error obteniendo datos del token: ", e);
            return crearFallback();
        }
    }

    private ColaboradorDTO crearFallback() {
        log.warn("⚠️ Creando fallback final");
        ColaboradorDTO dto = new ColaboradorDTO();
        dto.setNombreCompleto("Usuario");
        dto.setEmail("usuario@asmetsalud.com");
        dto.setCargo("Cargo no disponible");
        dto.setSede("Sede no disponible");
        dto.setDocumento("");
        return dto;
    }
}