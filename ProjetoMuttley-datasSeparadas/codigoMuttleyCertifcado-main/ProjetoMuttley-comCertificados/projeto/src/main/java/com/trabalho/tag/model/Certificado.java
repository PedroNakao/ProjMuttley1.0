package com.trabalho.tag.model;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Certificado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;
    private String descricao;
    private String tipo;

    // CORREÇÃO AQUI: Relação 1 para 1 apontando para a PK simples (id) de EventoParticipante
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "evento_participante_eventoid", referencedColumnName = "eventoId"),
        @JoinColumn(name = "evento_participante_participanteid", referencedColumnName = "participanteId")
    })
    private EventoParticipante eventoParticipante;
}