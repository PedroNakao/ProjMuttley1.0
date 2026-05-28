package com.trabalho.tag.service;

import com.trabalho.tag.model.Certificado;
import com.trabalho.tag.model.Evento;
import com.trabalho.tag.model.EventoParticipante;
import com.trabalho.tag.repository.CertificadoRepository;
import com.trabalho.tag.repository.EventoParticipanteRepository;
import com.trabalho.tag.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra a finalização de um evento:
 *  1. Marca o evento como finalizado
 *  2. Cria um Certificado para cada participante inscrito (se ainda não existir)
 *  3. Envia o certificado em PDF por e-mail para cada participante (assíncrono)
 */
@Service
public class EventoFinalizacaoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoParticipanteRepository eventoParticipanteRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private CertificadoEmailService certificadoEmailService;

    /**
     * Finaliza o evento e emite certificados.
     *
     * @param eventoId ID do evento a ser finalizado
     * @return Número de certificados gerados
     * @throws IllegalArgumentException se o evento não existir
     * @throws IllegalStateException    se o evento já estiver finalizado
     */
    @Transactional
    public int finalizarEvento(Long eventoId) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Evento não encontrado: ID " + eventoId));

        if (Boolean.TRUE.equals(evento.getFinalizado())) {
            throw new IllegalStateException(
                    "O evento \"" + evento.getTitulo() + "\" já foi finalizado.");
        }

        // Marca como finalizado
        evento.setFinalizado(true);
        eventoRepository.save(evento);

        // Busca todos os inscritos no evento
        List<EventoParticipante> inscritos =
                eventoParticipanteRepository.findByIdEventoId(eventoId);

        List<Certificado> certificadosNovos = new ArrayList<>();

        for (EventoParticipante ep : inscritos) {

            // Evita duplicata: verifica se certificado já existe para esse par
            boolean jaExiste = certificadoRepository
                    .findByEventoParticipante_Participante_IdAndEventoParticipante_Evento_Id(
                            ep.getParticipante().getId(), eventoId)
                    .isPresent();

            if (!jaExiste) {
                Certificado cert = new Certificado();
                cert.setTitulo("Certificado de Participação — " + evento.getTitulo());
                cert.setDescricao("Certificado emitido automaticamente ao concluir o evento \""
                        + evento.getTitulo() + "\".");
                cert.setTipo("de Participação");
                cert.setEventoParticipante(ep);

                certificadoRepository.save(cert);
                certificadosNovos.add(cert);
            }
        }

        // Envia e-mails de forma assíncrona (não bloqueia a resposta HTTP)
        for (Certificado cert : certificadosNovos) {
            certificadoEmailService.enviarCertificado(cert);
        }

        return certificadosNovos.size();
    }
}
