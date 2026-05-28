package com.trabalho.tag.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    // Tipo do evento (Palestra, Workshop, Aula Aberta, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    // Modalidade (Presencial, Remoto, Híbrido)
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", nullable = false)
    private ModalidadeEvento modalidade;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    // Carga horária em minutos (calculada automaticamente ou manual)
    @Column(name = "carga_horaria_minutos")
    private Integer cargaHorariaMinutos;

    // Local físico ou link da reunião
    @Column(length = 300)
    private String local;

    // Máximo de participantes (0 = ilimitado)
    @Column(name = "vagas_maximas")
    private Integer vagasMaximas = 0;

    // Semestre ao qual o evento está vinculado
    // Ex: "2026.1", "2026.2"
    @Column(name = "semestre_referencia", length = 10)
    private String semestreReferencia;

    // Disciplina relacionada ao evento
    @Column(name = "disciplina_relacionada", length = 150)
    private String disciplinaRelacionada;

    // Indica se o evento faz parte de um Fórum
    @Column(name = "e_forum")
    private Boolean eForum = false;

    // Indica se o evento concede medalha especial
    @Column(name = "concede_medalha_especial")
    private Boolean concedeMedalhaEspecial = false;

    // Pontos que o evento concede ao participante
    @Column(name = "pontos_participacao")
    private Integer pontosParticipacao = 1;

    // Auditoria
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // Indica se o evento já foi finalizado e os certificados emitidos
    @Column(name = "finalizado", nullable = false)
    private Boolean finalizado = false;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        calcularCargaHoraria();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
        calcularCargaHoraria();
    }

    // Calcula automaticamente a carga horária em minutos
    private void calcularCargaHoraria() {
        if (this.dataInicio != null && this.dataFim != null) {
            long minutos = java.time.Duration.between(this.dataInicio, this.dataFim).toMinutes();
            this.cargaHorariaMinutos = (int) minutos;
        }
    }

    @ManyToMany(mappedBy = "listaTag")
    private List<Tag> listaTag = new ArrayList<>();

    @OneToMany(mappedBy = "evento")
    private List<EventoParticipante> eventoParticipantes = new ArrayList<>();
}
