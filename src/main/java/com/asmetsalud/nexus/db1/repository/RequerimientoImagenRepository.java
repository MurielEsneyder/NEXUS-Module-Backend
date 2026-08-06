package com.asmetsalud.nexus.db1.repository;

import com.asmetsalud.nexus.db1.model.RequerimientoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequerimientoImagenRepository extends JpaRepository<RequerimientoImagen, Long> {

    List<RequerimientoImagen> findByRequerimientoIdOrderByOrdenAsc(Long requerimientoId);

    void deleteByRequerimientoId(Long requerimientoId);
}
