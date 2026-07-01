package com.asmetsalud.nexus.solicitudes.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🔍 Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                System.out.println("✅ Autenticado: " + authentication.isAuthenticated());
                Object principal = authentication.getPrincipal();
                System.out.println("👤 Principal: " + principal);

                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                }
                return principal.toString();
            }
            System.out.println("⚠️ No hay autenticación");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error en getCurrentUsername: " + e.getMessage());
            return null;
        }
    }
}