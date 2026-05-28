package com.trabalho.tag.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Palestrante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Palestrante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 150)
    private String email;

    // Título ou cargo do palestrante
    // Ex: "Professor", "Engenheiro de Software", "CEO"
    @Column(length = 150)
    private String titulo;

    // Breve bio/descrição
    @Column(length = 500)
    private String bio;

    // URL do perfil LinkedIn do palestrante
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    // URL do perfil GitHub do palestrante
    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToMany
    @JoinTable(
        name = "Evento_palestrante",
        joinColumns = @JoinColumn(name = "palestrante_id"),       // conforme DER
        inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    private List<Evento> listaEvento = new ArrayList<>();
}