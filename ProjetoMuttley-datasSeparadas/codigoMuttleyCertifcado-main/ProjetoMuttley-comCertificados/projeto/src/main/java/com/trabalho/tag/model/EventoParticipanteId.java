package com.trabalho.tag.model;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable; // Garanta essa importação
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventoParticipanteId implements Serializable {

    @Column(name = "Eventoid") // Padronizado com o padrão da tabela intermediária
    private Long eventoId;
    
    @Column(name = "Participanteid") // Padronizado
    private Long participanteId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoParticipanteId that)) return false;
        return Objects.equals(eventoId, that.eventoId) &&
               Objects.equals(participanteId, that.participanteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventoId, participanteId);
    }    
}