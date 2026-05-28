package com.trabalho.tag.controller;

import com.trabalho.tag.model.*;
import com.trabalho.tag.repository.*;
import com.trabalho.tag.service.CertificadoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/participante")
public class InscricaoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private EventoParticipanteRepository eventoParticipanteRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private CertificadoEmailService certificadoEmailService;

    @GetMapping("/cadastrar")
    public String telaInscricao(@RequestParam(value = "eventoId", required = false) Long eventoId, Model model) {
        if (eventoId == null) {
            model.addAttribute("mensagem", "Erro: ID do evento não informado.");
            return "participante/inscricao";
        }
        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        model.addAttribute("evento", evento);
        return "participante/inscricao";
    }

    // ── FLUXO 1: APENAS CONFIRMAR PRESENÇA (Aba: Já sou cadastrado) ─────────────────
    @PostMapping("/confirmar-presenca")
    public String confirmarPresenca(
            @RequestParam("eventoId") Long eventoId,
            @RequestParam("cpf") String cpf,
            Model model) {

        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        model.addAttribute("evento", evento);
        model.addAttribute("cpfDigitado", cpf.trim());
        model.addAttribute("abaAtiva", "presenca");

        if (evento == null) {
            model.addAttribute("mensagem", "Erro: Evento não encontrado.");
            return "participante/inscricao";
        }

        try {
            Participante participante = participanteRepository.findByCpf(cpf.trim()).orElse(null);

            if (participante == null) {
                model.addAttribute("mensagem", "Erro: CPF não cadastrado. Por favor, utilize a aba de Novo Cadastro.");
                return "participante/inscricao";
            }

            // Monta ID composto e valida duplicidade
            EventoParticipanteId idComposto = new EventoParticipanteId();
            idComposto.setEventoId(evento.getId());
            idComposto.setParticipanteId(participante.getId());

            if (eventoParticipanteRepository.existsById(idComposto)) {
                model.addAttribute("mensagem", "Ops! Você já registrou presença neste evento.");
                return "participante/inscricao";
            }

            // Salva a presença e gera pontos/certificado
            salvarInscricaoCompleta(evento, participante, idComposto);

            model.addAttribute("cpfDigitado", "");
            model.addAttribute("mensagem", "✅ " + participante.getNome() + ", presença confirmada! Certificado gerado!");

        } catch (Exception ex) {
            model.addAttribute("mensagem", "Erro ao processar presença: " + ex.getMessage());
        }
        return "participante/inscricao";
    }

    // ── FLUXO 2: NOVO CADASTRO + INSCRIÇÃO (Aba: Novo cadastro) ─────────────────────
    @PostMapping("/cadastrar-e-inscrever")
    public String cadastrarEInscrever(
            @RequestParam("eventoId") Long eventoId,
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("email") String email,
            @RequestParam(value = "linkedin", required = false) String linkedin,
            Model model) {

        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        model.addAttribute("evento", evento);
        model.addAttribute("abaAtiva", "cadastro");

        if (evento == null) {
            model.addAttribute("mensagem", "Erro: Evento não encontrado.");
            return "participante/inscricao";
        }

        try {
            Participante participante = participanteRepository.findByCpf(cpf.trim()).orElse(null);

            // Se o usuário tentar se cadastrar de novo mas já existir no banco
            if (participante != null) {
                model.addAttribute("mensagem", "Ops! Esse CPF já possui cadastro. Use a aba 'Já sou cadastrado'.");
                model.addAttribute("abaAtiva", "presenca"); // Manda ele de volta pra aba certa
                model.addAttribute("cpfDigitado", cpf.trim());
                return "participante/inscricao";
            }

            // Criando o novo participante de fato
            participante = new Participante();
            participante.setNome(nome.trim());
            participante.setCpf(cpf.trim());
            participante.setEmail(email.trim());
            participante.setLinkedin(linkedin);
            participante.setPontos(0);
            participante = participanteRepository.save(participante);

            // Vincula Chave Composta
            EventoParticipanteId idComposto = new EventoParticipanteId();
            idComposto.setEventoId(evento.getId());
            idComposto.setParticipanteId(participante.getId());

            salvarInscricaoCompleta(evento, participante, idComposto);

            model.addAttribute("mensagem", "✅ Cadastro realizado com sucesso! Presença confirmada!");
            model.addAttribute("abaAtiva", "presenca");

        } catch (Exception ex) {
            model.addAttribute("mensagem", "Erro ao realizar cadastro: " + ex.getMessage());
        }
        return "participante/inscricao";
    }

    private void salvarInscricaoCompleta(Evento evento, Participante participante, EventoParticipanteId idComposto) {
        // 1. Salva o vínculo da inscrição primeiro
        EventoParticipante inscricao = new EventoParticipante();
        inscricao.setId(idComposto);
        inscricao.setEvento(evento);
        inscricao.setParticipante(participante);
        inscricao = eventoParticipanteRepository.save(inscricao);

        // 2. Formata a data do evento para ficar bonita no texto (Ex: 12/03/2026)
        java.time.format.DateTimeFormatter formatador = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada = evento.getDataInicio() != null ? evento.getDataInicio().format(formatador) : "Data não informada";

        // 3. Pega a descrição do Tipo de Evento (Ex: Palestra, Workshop)
        String tipoDescricao = (evento.getTipoEvento() != null) ? evento.getTipoEvento().getDescricao() : "Evento";

        // 4. Monta os textos dinâmicos e completos
        Certificado certificado = new Certificado();
        certificado.setTitulo("Certificado de Participação — " + evento.getTitulo());
        
        String textoDescricao = String.format(
            "Certificamos que %s participou do(a) %s '%s' realizado em %s, acumulando %d pontos de participação.",
            participante.getNome(),
            tipoDescricao,
            evento.getTitulo(),
            dataFormatada,
            evento.getPontosParticipacao() != null ? evento.getPontosParticipacao() : 0
        );
        certificado.setDescricao(textoDescricao);
        certificado.setTipo("Automático");
        
        // Vincula a inscrição salva que contém a chave mapeada
        certificado.setEventoParticipante(inscricao);
        
        // Salva o certificado com os metadados corrigidos
        certificadoRepository.save(certificado);

        // Dispara o envio do certificado por e-mail (assíncrono — não bloqueia a resposta)
        certificadoEmailService.enviarCertificado(certificado);

        // 5. Atualiza os pontos do participante
        int pontos = evento.getPontosParticipacao() != null ? evento.getPontosParticipacao() : 0;
        participante.setPontos(participante.getPontos() + pontos);
        participanteRepository.save(participante);
    }
}