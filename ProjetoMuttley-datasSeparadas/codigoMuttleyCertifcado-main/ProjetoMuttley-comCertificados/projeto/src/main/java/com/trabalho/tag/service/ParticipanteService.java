package com.trabalho.tag.service;

import com.trabalho.tag.model.*;
import com.trabalho.tag.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipanteService {

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private EventoParticipanteRepository eventoParticipanteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private CertificadoEmailService certificadoEmailService;

 // Registro de presença e pontuação do aluno via QR Code (Tela 1).
    @Transactional
    public String registrarPresencaPorQrCode(Long eventoId, String cpf) {

        // 1. Verifica se o ID capturado pelo QR Code realmente condiz com um evento válido no sistema.
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));

        // 2. Busca o participante no banco com base no CPF digitado na tela rápida.
        Participante participante = participanteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("CPF não cadastrado. Por favor, realize o cadastro no evento primeiro."));

        // 3. Monta e instancia a Chave Composta obrigatória para a tabela intermediária
        EventoParticipanteId idComposto = new EventoParticipanteId();
        idComposto.setEventoId(evento.getId());
        idComposto.setParticipanteId(participante.getId());

        // 4. Checa se este aluno já registrou presença usando os campos da chave composta (id.eventoId e id.participanteId)
        boolean jaParticipou = eventoParticipanteRepository.existsById(idComposto);
        if (jaParticipou) {
            return "Ops! Você já registrou presença e ganhou os pontos deste evento.";
        }

        // 5. Cria o vínculo Evento ↔ Participante injetando a chave primária composta
        EventoParticipante novaInscricao = new EventoParticipante();
        novaInscricao.setId(idComposto); // <--- ISSO AQUI É O QUE FALTAVA!
        novaInscricao.setEvento(evento);
        novaInscricao.setParticipante(participante);
        
        // Salva a base primeiro para que ela ganhe existência física no banco
        novaInscricao = eventoParticipanteRepository.save(novaInscricao);

        // 6. Gera e persiste o certificado apontando para a inscrição salva
        Certificado certificado = new Certificado();
        certificado.setTitulo("Certificado de Participação: " + evento.getTitulo());
        certificado.setDescricao("Certificado gerado automaticamente pela confirmação de presença no evento.");
        certificado.setTipo("Automático");
        certificado.setEventoParticipante(novaInscricao);
        certificadoRepository.save(certificado);

        // Dispara o envio do certificado por e-mail (assíncrono — não bloqueia a resposta)
        certificadoEmailService.enviarCertificado(certificado);

        // 7. Soma os pontos do evento no total do participante
        int pontosDoEvento = (evento.getPontosParticipacao() != null) ? evento.getPontosParticipacao() : 0;
        participante.setPontos(participante.getPontos() + pontosDoEvento);
        participanteRepository.save(participante);

        return "Presença confirmada! Pontos adicionados: " + pontosDoEvento
                + ". Total atual: " + participante.getPontos()
                + ". Seu certificado foi gerado automaticamente!";
    }

    // Primeiro cadastro isolado (endpoint /participantes/cadastrar).
    @Transactional
    public String cadastrarNovoParticipante(String nome, String cpf, String email, String linkedin) {

        if (participanteRepository.findByCpf(cpf).isPresent()) {
            return "Erro: Este CPF já está cadastrado no sistema!";
        }

        Participante novoParticipante = new Participante();
        novoParticipante.setNome(nome);
        novoParticipante.setCpf(cpf);
        novoParticipante.setEmail(email);
        novoParticipante.setLinkedin(linkedin);
        novoParticipante.setPontos(0);
        participanteRepository.save(novoParticipante);

        return "Cadastro realizado com sucesso! Volte para a tela anterior e digite seu CPF para garantir sua presença.";
    }
}
