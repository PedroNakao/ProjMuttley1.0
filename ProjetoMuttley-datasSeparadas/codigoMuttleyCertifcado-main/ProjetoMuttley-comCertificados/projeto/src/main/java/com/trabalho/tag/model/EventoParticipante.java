package com.trabalho.tag.model;

import java.io.Serializable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
}
