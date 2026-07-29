package com.asmetsalud.nexus.db1.repository;

import com.asmetsalud.nexus.db1.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findBySolicitudIdOrderByCreatedAtDesc(Long solicitudId);

    @Query("SELECT a FROM Auditoria a WHERE a.solicitud.id = ?1 ORDER BY a.createdAt DESC")
    List<Auditoria> findAuditoriasBySolicitudId(Long solicitudId);

    @Query("SELECT COUNT(a) FROM Auditoria a WHERE a.solicitud.id = ?1")
    Long countBySolicitudId(Long solicitudId);
}
