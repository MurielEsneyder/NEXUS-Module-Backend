package com.asmetsalud.nexus.solicitudes.repository;

import com.asmetsalud.nexus.solicitudes.entity.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoSolicitudRepository extends JpaRepository<TipoSolicitud, Long> {

    List<TipoSolicitud> findByActivoTrue();

    Optional<TipoSolicitud> findByCodigo(String codigo);
}