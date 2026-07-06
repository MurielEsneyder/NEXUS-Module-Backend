package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.Requerimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequerimientoRepository extends JpaRepository<Requerimiento, Long> {

    List<Requerimiento> findBySolicitudId(Long solicitudId);

    List<Requerimiento> findBySolicitudIdAndTipoRequerimiento(Long solicitudId, Short tipoRequerimiento);

    @Query("SELECT MAX(r.numeroOrden) FROM Requerimiento r WHERE r.solicitud.id = ?1 AND r.tipoRequerimiento = ?2")
    Integer findMaxNumeroOrden(Long solicitudId, Short tipoRequerimiento);

    @Query("SELECT COUNT(r) FROM Requerimiento r WHERE r.solicitud.id = ?1")
    Integer countBySolicitudId(Long solicitudId);

    @Query("SELECT COUNT(r) FROM Requerimiento r WHERE r.solicitud.id = ?1 AND r.tipoRequerimiento = ?2")
    Integer countBySolicitudIdAndTipoRequerimiento(Long solicitudId, Short tipoRequerimiento);
}