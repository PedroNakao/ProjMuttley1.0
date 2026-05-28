package com.trabalho.tag.controller;

import com.trabalho.tag.model.Evento;
import com.trabalho.tag.repository.EventoParticipanteRepository;
import com.trabalho.tag.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoParticipanteRepository eventoParticipanteRepository;

    // ── Tela do dashboard ─────────────────────────────────────────────────────
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // ── API: lista de eventos para o dashboard ────────────────────────────────
    @GetMapping("/api/admin/eventos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarEventos() {

        List<Evento> eventos = eventoRepository.findAll();

        // Total de inscrições em todos os eventos
        long totalParticipantes = eventoParticipanteRepository.count();

        List<Map<String, Object>> listaDTO = eventos.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",                  e.getId());
            m.put("titulo",              e.getTitulo());
            m.put("descricao",           e.getDescricao());
            m.put("tipoEvento",          e.getTipoEvento() != null ? e.getTipoEvento().name() : null);
            m.put("tipoEventoDescricao", e.getTipoEvento() != null ? e.getTipoEvento().getDescricao() : null);
            m.put("modalidade",          e.getModalidade() != null ? e.getModalidade().name() : null);
            m.put("dataInicio",          e.getDataInicio() != null ? e.getDataInicio().toString() : null);
            m.put("dataFim",             e.getDataFim()    != null ? e.getDataFim().toString()    : null);
            m.put("cargaHorariaMinutos", e.getCargaHorariaMinutos());
            m.put("local",               e.getLocal());
            m.put("vagasMaximas",        e.getVagasMaximas());
            m.put("semestreReferencia",  e.getSemestreReferencia());
            m.put("pontosParticipacao",  e.getPontosParticipacao()); // Ajuste se o get do lombok mudar
            m.put("eForum",              e.getEForum());
            m.put("concedeMedalhaEspecial", e.getConcedeMedalhaEspecial());
            m.put("ativo",               e.getAtivo());

            // CORREÇÃO AQUI: Mudou para findByIdEventoId por causa da chave composta embutida
            long inscritos = eventoParticipanteRepository.findByIdEventoId(e.getId()).size();
            m.put("totalInscritos", inscritos);

            // Tags
            List<String> tagNomes = e.getListaTag() != null
                    ? e.getListaTag().stream().map(t -> t.getNome()).collect(Collectors.toList())
                    : Collections.emptyList();
            m.put("tags", tagNomes);

            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eventos", listaDTO);
        result.put("totalParticipantes", totalParticipantes);

        return ResponseEntity.ok(result);
    }
}