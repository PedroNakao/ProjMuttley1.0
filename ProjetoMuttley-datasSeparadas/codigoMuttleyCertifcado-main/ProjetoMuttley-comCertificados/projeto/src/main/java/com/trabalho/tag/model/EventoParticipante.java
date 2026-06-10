package com.trabalho.tag.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Evento_Participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoParticipante implements Serializable {

    @EmbeddedId
    private EventoParticipanteId id = new EventoParticipanteId();

    @ManyToOne
    @MapsId("eventoId")
    @JoinColumn(name = "Eventoid")
    private Evento evento;

    @ManyToOne
    @MapsId("participanteId")
    @JoinColumn(name = "Participanteid")
    private Participante participante;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean inscrito;
}
