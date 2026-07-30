package com.asmetsalud.nexus.db1.repository;

import com.asmetsalud.nexus.db1.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface    AreaRepository extends JpaRepository<Area, Long> {

    @Override
    @Query(value = "SELECT lv.* FROM com_lista_valores lv JOIN com_listas_listavalores llv ON lv.id_lista_valor = llv.id_lista_valor WHERE llv.id_lista = 10 ORDER BY lv.orden", nativeQuery = true)
    List<Area> findAll();
}
