package com.asmetsalud.nexus.solicitudes.controller;

import com.asmetsalud.nexus.solicitudes.dto.ColaboradorDTO;
import com.asmetsalud.nexus.solicitudes.service.ColaboradorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/colaborador")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    @GetMapping("/actual")
    public ResponseEntity<ColaboradorDTO> obtenerColaboradorActual() {
        log.info("📥 Obteniendo datos del colaborador actual");
        ColaboradorDTO colaborador = colaboradorService.obtenerColaboradorActual();
        return ResponseEntity.ok(colaborador);
    }
}