package com.trabalho.tag.controller;
 
import com.trabalho.tag.model.*;
import com.trabalho.tag.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
 
@Controller
@RequestMapping("/presenca")
public class PresencaController {
 
    @Autowired
    private EventoRepository eventoRepository;
 
    @Autowired
    private ParticipanteRepository participanteRepository;
 
    @Autowired
    private EventoParticipanteRepository eventoParticipanteRepository;
 
    /**
     * Tela de confirmação de presença — acessada pelo QR Code exibido durante o evento.
     * GET /presenca/{eventoId}
     */
    @GetMapping("/{eventoId}")
    public String telaPresenca(@PathVariable Long eventoId, Model model) {
        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        model.addAttribute("evento", evento);
        return "participante/presenca";
    }
 
    /**
     * Processa a confirmação de presença pelo CPF.
     * POST /presenca/{eventoId}
     */
    @PostMapping("/{eventoId}")
    public String confirmarPresenca(
            @PathVariable Long eventoId,
            @RequestParam("cpf") String cpf,
            Model model) {
 
        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        model.addAttribute("evento", evento);
 
        if (evento == null) {
            model.addAttribute("erro", "Evento não encontrado.");
            return "participante/presenca";
        }
 
        try {
            String cpfLimpo = cpf.trim().replaceAll("[^0-9]", "");
 
            // Busca o participante pelo CPF
            Participante participante = participanteRepository.findByCpf(cpfLimpo).orElse(null);
            if (participante == null) {
                model.addAttribute("erro", "CPF não encontrado. Você precisa se inscrever primeiro pelo link de inscrição do evento.");
                return "participante/presenca";
            }
 
            // Busca o vínculo EventoParticipante
            EventoParticipanteId idComposto = new EventoParticipanteId();
            idComposto.setEventoId(eventoId);
            idComposto.setParticipanteId(participante.getId());
 
            EventoParticipante ep = eventoParticipanteRepository.findById(idComposto).orElse(null);
            if (ep == null) {
                model.addAttribute("erro", "Você não está inscrito neste evento. Inscreva-se primeiro pelo link de inscrição.");
                return "participante/presenca";
            }
 
            // Já confirmou presença anteriormente
            if (Boolean.TRUE.equals(ep.getInscrito())) {
                model.addAttribute("aviso", "Sua presença já foi confirmada anteriormente, " + participante.getNome() + "!");
                return "participante/presenca";
            }
 
            // Marca presença: inscrito = true
            ep.setInscrito(true);
            eventoParticipanteRepository.save(ep);
 
            model.addAttribute("sucesso", "✅ Presença confirmada com sucesso, " + participante.getNome() + "!");
 
        } catch (Exception ex) {
            model.addAttribute("erro", "Erro ao confirmar presença: " + ex.getMessage());
        }
 
        return "participante/presenca";
    }
}
 