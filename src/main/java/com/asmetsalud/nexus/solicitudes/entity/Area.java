package com.asmetsalud.nexus.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "com_lista_valores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lista_valor")
    private Long id;

    @Column(name = "descripcion", nullable = false)
    private String nombre;

    @Column(name = "orden")
    private Long orden;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}
