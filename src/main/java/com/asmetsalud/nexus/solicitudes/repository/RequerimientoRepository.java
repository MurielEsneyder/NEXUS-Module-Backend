package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.Requerimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequerimientoRepository extends JpaRepository<Requerimiento, Long> {

    List<Requerimiento> findBySolicitudId(Long solicitudId);

    List<Requerimiento> findBySolicitudIdAndTipoRequerimiento(Long solicitudId, Short tipoRequerimiento);

    @Query("SELECT MAX(r.numeroOrden) FROM Requerimiento r WHERE r.solicitud.id = :solicitudId AND r.tipoRequerimiento = :tipo")
    Integer findMaxNumeroOrdenBySolicitudIdAndTipo(@Param("solicitudId") Long solicitudId,
                                                   @Param("tipo") Short tipo);

    // ============================================================
    // NUEVO MÉTODO PARA CONTAR REQUERIMIENTOS POR SOLICITUD
    // ============================================================
    @Query("SELECT COUNT(r) FROM Requerimiento r WHERE r.solicitud.id = :solicitudId")
    int countBySolicitudId(@Param("solicitudId") Long solicitudId);
}