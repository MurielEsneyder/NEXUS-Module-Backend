package com.asmetsalud.nexus.db1.repository;

import com.asmetsalud.nexus.db1.model.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoSolicitudRepository extends JpaRepository<TipoSolicitud, Long> {

    List<TipoSolicitud> findByActivoTrue();

    Optional<TipoSolicitud> findByCodigo(String codigo);
}
