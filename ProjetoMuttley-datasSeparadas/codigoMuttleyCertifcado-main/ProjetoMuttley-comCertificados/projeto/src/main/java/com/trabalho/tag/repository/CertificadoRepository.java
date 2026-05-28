package com.trabalho.tag.repository;

import com.trabalho.tag.model.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    // Todos os certificados de um participante (via eventoParticipante)
    List<Certificado> findByEventoParticipante_Participante_Id(Long participanteId);

    // Certificado específico de um participante num evento
    Optional<Certificado> findByEventoParticipante_Participante_IdAndEventoParticipante_Evento_Id(
            Long participanteId, Long eventoId);
}
