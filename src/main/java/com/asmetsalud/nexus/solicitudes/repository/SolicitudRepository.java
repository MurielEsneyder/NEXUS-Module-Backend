package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByCodigo(String codigo);

    List<Solicitud> findByEmpleadoDocumento(String empleadoDocumento);

    List<Solicitud> findByEstadoId(Long estadoId);

    @Query("SELECT s FROM Solicitud s WHERE s.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Solicitud> findByFechaCreacionBetween(@Param("fechaInicio") LocalDate fechaInicio,
                                               @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COUNT(s) FROM Solicitud s WHERE s.estado.id = :estadoId")
    Long countByEstadoId(@Param("estadoId") Long estadoId);
}