package com.trabalho.tag.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Patrocinador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patrocinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    // Nível do patrocínio: "Ouro", "Prata", "Bronze", "Apoio"
    @Column(name = "nivel_patrocinio", length = 50)
    private String nivelPatrocinio;

    @Column(length = 255)
    private String site;

    @Column(length = 255)
    private String logoUrl;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToMany
    @JoinTable(
        name = "Evento_patrocinador",
        joinColumns = @JoinColumn(name = "patrocinador_id"),          // conforme DER
        inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    private List<Evento> eventos = new ArrayList<>();
}
