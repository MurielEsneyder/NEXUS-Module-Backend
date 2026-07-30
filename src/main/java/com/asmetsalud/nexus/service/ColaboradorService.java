package com.asmetsalud.nexus.service;

import com.asmetsalud.nexus.dto.ColaboradorDTO;
import com.asmetsalud.nexus.db2.repository.consultaColaborador.ColaboradorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorDTO obtenerColaboradorActual() {
        log.info("🔍 Obteniendo datos del colaborador desde BD Oracle");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            log.info("👤 Username obtenido: {}", username);

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

            final String finalEmailPrefix = (email != null && email.contains("@")) 
                    ? email.substring(0, email.indexOf("@")) 
                    : email;

            log.info("🔍 Buscando por email: {}@%", finalEmailPrefix);

            ColaboradorDTO dto = colaboradorRepository.buscarPorEmailPrefix(finalEmailPrefix)
                    .orElseThrow(() -> new RuntimeException("No se encontró el colaborador con el email prefix: " + finalEmailPrefix));

            log.info("✅ Datos obtenidos de BD: {} - {}", dto.getNombreCompleto(), dto.getEmail());
            return dto;

        } catch (Exception e) {
            log.error("❌ Error consultando BD Oracle: ", e);
            throw new RuntimeException("Error al consultar datos de colaborador en BD Oracle: " + e.getMessage(), e);
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
