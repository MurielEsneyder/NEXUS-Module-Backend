package com.asmetsalud.nexus.db2.repository.consultaColaborador;

import com.asmetsalud.nexus.dto.ColaboradorDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Map;
import java.util.Optional;

@Repository
public class ColaboradorRepository {

    private final JdbcTemplate db2JdbcTemplate;

    public ColaboradorRepository(@Qualifier("db2JdbcTemplate") JdbcTemplate db2JdbcTemplate) {
        this.db2JdbcTemplate = db2JdbcTemplate;
    }

    public Optional<ColaboradorDTO> buscarPorEmailPrefix(String emailPrefix) {
        String sql = """
            SELECT
                p.ID_PERSONA,
                TRIM(NVL(p.NOMBRE, '') || ' ' ||
                     NVL(p.PRIMER_APELLIDO, '') || ' ' ||
                     NVL(p.SEGUNDO_APELLIDO, '')) AS NOMBRE_COMPLETO,
                p.EMAIL AS CORREO,
                p.NOR_IDENTIFICACION AS IDENTIFICACION,
                e.COD_USER,
                c.CARGO_NOMBRE AS CARGO,
                s.NOMBRE_SEDE AS SEDE
            FROM PAR_PERSONA p
            LEFT JOIN PAR_EMPLEADO_EPS e ON p.ID_PERSONA = e.ID_PERSONA
            LEFT JOIN PAR_CARGO c ON e.ID_CARGO = c.ID_CARGO
            LEFT JOIN PAR_SEDE s ON e.ID_SEDE = s.ID_SEDE
            WHERE p.ESTADO <> 'R'
              AND p.EMAIL LIKE ? || '@%'
        """;

        try {
            Map<String, Object> result = db2JdbcTemplate.queryForMap(sql, emailPrefix);

            ColaboradorDTO dto = new ColaboradorDTO();
            dto.setNombreCompleto((String) result.get("NOMBRE_COMPLETO"));
            dto.setEmail((String) result.get("CORREO"));
            dto.setDocumento((String) result.get("IDENTIFICACION"));
            dto.setCargo((String) result.get("CARGO"));
            dto.setSede((String) result.get("SEDE"));
            dto.setIdPersona(result.get("ID_PERSONA") != null ? ((Number) result.get("ID_PERSONA")).longValue() : null);
            dto.setCodUser((String) result.get("COD_USER"));

            return Optional.of(dto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
