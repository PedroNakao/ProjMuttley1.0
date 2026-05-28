package com.trabalho.tag.repository;

import com.trabalho.tag.model.EventoParticipante;
import com.trabalho.tag.model.EventoParticipanteId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventoParticipanteRepository extends JpaRepository<EventoParticipante, EventoParticipanteId> {
    
    // CORREÇÃO: "IdEventoId" diz ao Spring para buscar dentro de 'id.eventoId'
    List<EventoParticipante> findByIdEventoId(Long eventoId);
    
    // CORREÇÃO: "existsById..." mapeia direto para as propriedades da chave composta
    boolean existsByIdParticipanteIdAndIdEventoId(Long participanteId, Long eventoId);
}