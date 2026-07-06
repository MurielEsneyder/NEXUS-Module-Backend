package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Long> {

    List<EstadoSolicitud> findByActivoTrue();

    Optional<EstadoSolicitud> findByCodigo(String codigo);

    Optional<EstadoSolicitud> findByNombre(String nombre);
}