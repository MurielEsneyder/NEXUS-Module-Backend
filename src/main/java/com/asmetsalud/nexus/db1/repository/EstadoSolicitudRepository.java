package com.asmetsalud.nexus.db1.repository;

import com.asmetsalud.nexus.db1.model.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Long> {

    @Override
    @Query(value = "SELECT * FROM sd_estado_solicitud WHERE id >= 59 ORDER BY id", nativeQuery = true)
    List<EstadoSolicitud> findAll();

    @Query(value = "SELECT * FROM sd_estado_solicitud WHERE id >= 59 AND activo = true ORDER BY id", nativeQuery = true)
    List<EstadoSolicitud> findByActivoTrue();

    Optional<EstadoSolicitud> findByCodigo(String codigo);

    Optional<EstadoSolicitud> findByNombre(String nombre);
}
