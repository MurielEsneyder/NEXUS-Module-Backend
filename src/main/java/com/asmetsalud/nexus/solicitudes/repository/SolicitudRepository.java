package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.Solicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByCodigo(String codigo);

    List<Solicitud> findByEmpleadoDocumento(String empleadoDocumento);

    Page<Solicitud> findByEmpleadoDocumentoOrderByFechaCreacionDesc(String empleadoDocumento, Pageable pageable);

    List<Solicitud> findByEstadoId(Long estadoId);

    @Query("SELECT COUNT(s) FROM Solicitud s WHERE s.estado.id = ?1")
    Long countByEstadoId(Long estadoId);

    @Query("SELECT s FROM Solicitud s ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findAllOrderByFechaCreacionDesc();

    @Query("SELECT s FROM Solicitud s WHERE s.estado.id NOT IN (7, 8) ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findActiveSolicitudes();
}