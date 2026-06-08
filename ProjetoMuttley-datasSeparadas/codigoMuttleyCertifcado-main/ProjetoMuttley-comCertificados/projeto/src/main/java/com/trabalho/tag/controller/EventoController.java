package com.trabalho.tag.controller;

import com.trabalho.tag.dto.DropdownsEventoDTO;
import com.trabalho.tag.model.*;
import com.trabalho.tag.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PalestranteRepository palestranteRepository;

    @Autowired
    private PatrocinadorRepository patrocinadorRepository;

    // ── Tela de cadastro ─────────────────────────────────────────────────────

    @GetMapping("/eventos/cadastrar")
    public String paginaCadastrarEvento() {
        return "evento/cadastrar";
    }

    // ── POST: salva o evento ──────────────────────────────────────────────────

    @PostMapping("/eventos/cadastrar")
    public String cadastrarEvento(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("tipoEvento") TipoEvento tipoEvento,
            @RequestParam("modalidade") ModalidadeEvento modalidade,
            @RequestParam("dataInicio") String dataInicio,
            @RequestParam("dataFim") String dataFim,
            @RequestParam(value = "local", required = false) String local,
            @RequestParam(value = "vagasMaximas", defaultValue = "0") Integer vagasMaximas,
            @RequestParam(value = "pontosParticipacao", defaultValue = "1") Integer pontosParticipacao,
            @RequestParam(value = "semestreReferencia", required = false) String semestreReferencia,
            @RequestParam(value = "disciplinaRelacionada", required = false) String disciplinaRelacionada,
            @RequestParam(value = "eForum", required = false) String eForumParam,
            @RequestParam(value = "concedeMedalhaEspecial", required = false) String medalhaParam,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "palestranteIds", required = false) List<Long> palestranteIds,
            @RequestParam(value = "patrocinadorIds", required = false) List<Long> patrocinadorIds,
            Model model) {

        try {
            Evento evento = new Evento();
            evento.setTitulo(titulo);
            evento.setDescricao(descricao);
            evento.setTipoEvento(tipoEvento);
            evento.setModalidade(modalidade);
            evento.setDataInicio(LocalDateTime.parse(dataInicio));
            evento.setDataFim(LocalDateTime.parse(dataFim));
            evento.setLocal(local);
            evento.setVagasMaximas(vagasMaximas != null ? vagasMaximas : 0);
            boolean concederMedalha = "true".equals(medalhaParam);
            if (concederMedalha) {
                evento.setPontosParticipacao(0); // Se for medalha especial, zera os pontos obrigatoriamente
            } else {
                evento.setPontosParticipacao(pontosParticipacao != null ? pontosParticipacao : 1);
            }
            evento.setPontosParticipacao(pontosParticipacao != null ? pontosParticipacao : 1);
            evento.setSemestreReferencia(semestreReferencia);
            evento.setDisciplinaRelacionada(disciplinaRelacionada);
            evento.setEForum("true".equals(eForumParam));
            evento.setConcedeMedalhaEspecial("true".equals(medalhaParam));
            evento.setAtivo(true);

            eventoRepository.save(evento);

            // Associa tags
            if (tagIds != null && !tagIds.isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(tagIds);
                for (Tag t : tags) {
                    t.getListaTag().add(evento);
                    tagRepository.save(t);
                }
            }

            // Associa palestrantes
            if (palestranteIds != null && !palestranteIds.isEmpty()) {
                List<Palestrante> palestrantes = palestranteRepository.findAllById(palestranteIds);
                for (Palestrante p : palestrantes) {
                    p.getListaEvento().add(evento);
                    palestranteRepository.save(p);
                }
            }

            // Associa patrocinadores
            if (patrocinadorIds != null && !patrocinadorIds.isEmpty()) {
                List<Patrocinador> patrocinadores = patrocinadorRepository.findAllById(patrocinadorIds);
                for (Patrocinador p : patrocinadores) {
                    p.getEventos().add(evento);
                    patrocinadorRepository.save(p);
                }
            }

            model.addAttribute("mensagem", "Evento \"" + titulo + "\" cadastrado com sucesso! ID: " + evento.getId());

        } catch (Exception e) {
            model.addAttribute("mensagem", "Erro ao cadastrar evento: " + e.getMessage());
        }

        return "evento/cadastrar";
    }

    // ── API: dropdowns para o frontend ────────────────────────────────────────

    @GetMapping("/api/eventos/dropdowns")
    @ResponseBody
    public ResponseEntity<DropdownsEventoDTO> getDropdowns() {

        List<DropdownsEventoDTO.OpcaoEnumDTO> tiposEvento = Arrays.stream(TipoEvento.values())
                .map(DropdownsEventoDTO::fromTipoEvento)
                .collect(Collectors.toList());

        List<DropdownsEventoDTO.OpcaoEnumDTO> modalidades = Arrays.stream(ModalidadeEvento.values())
                .map(DropdownsEventoDTO::fromModalidade)
                .collect(Collectors.toList());

        List<DropdownsEventoDTO.TagOpcaoDTO> tags = tagRepository.findAll().stream()
                .map(t -> new DropdownsEventoDTO.TagOpcaoDTO(t.getId(), t.getNome(), ""))
                .collect(Collectors.toList());

        List<DropdownsEventoDTO.PalestranteOpcaoDTO> palestrantes = palestranteRepository
                .findByAtivoTrueOrderByNomeAsc().stream()
                .map(p -> new DropdownsEventoDTO.PalestranteOpcaoDTO(p.getId(), p.getNome(), p.getTitulo()))
                .collect(Collectors.toList());

        List<DropdownsEventoDTO.PatrocinadorOpcaoDTO> patrocinadores = patrocinadorRepository
                .findByAtivoTrueOrderByNomeAsc().stream()
                .map(p -> new DropdownsEventoDTO.PatrocinadorOpcaoDTO(p.getId(), p.getNome(), p.getNivelPatrocinio()))
                .collect(Collectors.toList());

        DropdownsEventoDTO dto = new DropdownsEventoDTO(tiposEvento, modalidades, tags, null, palestrantes, patrocinadores);
        return ResponseEntity.ok(dto);
    }

    // ── API: cadastrar palestrante inline (chamado pelo JS da tela de evento) ─

    @PostMapping("/api/palestrantes/criar")
    @ResponseBody
    public ResponseEntity<?> criarPalestrante(
            @RequestParam("nome") String nome,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "githubUrl", required = false) String githubUrl) {

        try {
            if (nome == null || nome.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Nome é obrigatório."));
            }
            Palestrante p = new Palestrante();
            p.setNome(nome.trim());
            p.setTitulo(titulo);
            p.setEmail(email);
            p.setBio(bio);
            p.setLinkedinUrl(linkedinUrl);
            p.setGithubUrl(githubUrl);
            p.setAtivo(true);
            palestranteRepository.save(p);

            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "nome", p.getNome(),
                    "titulo", p.getTitulo() != null ? p.getTitulo() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", e.getMessage()));
        }
    }

    // ── API: cadastrar patrocinador inline ────────────────────────────────────

    @PostMapping("/api/patrocinadores/criar")
    @ResponseBody
    public ResponseEntity<?> criarPatrocinador(
            @RequestParam("nome") String nome,
            @RequestParam(value = "nivelPatrocinio", required = false) String nivelPatrocinio,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "logoUrl", required = false) String logoUrl,
            @RequestParam(value = "email", required = false) String email) {

        try {
            if (nome == null || nome.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Nome é obrigatório."));
            }
            Patrocinador p = new Patrocinador();
            p.setNome(nome.trim());
            p.setNivelPatrocinio(nivelPatrocinio);
            p.setSite(site);
            p.setLogoUrl(logoUrl);
            p.setEmail(email);
            p.setAtivo(true);
            patrocinadorRepository.save(p);

            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "nome", p.getNome(),
                    "nivelPatrocinio", p.getNivelPatrocinio() != null ? p.getNivelPatrocinio() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", e.getMessage()));
        }
    }

    // ── GET: tela de edição ───────────────────────────────────────────────────

    @GetMapping("/eventos/editar/{id}")
    public String paginaEditarEvento(@PathVariable Long id, Model model) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + id));
        model.addAttribute("evento", evento);

        // IDs de tags, palestrantes e patrocinadores já vinculados
        List<Long> tagIdsSelecionadas = evento.getListaTag() != null
                ? evento.getListaTag().stream().map(Tag::getId).collect(Collectors.toList())
                : List.of();
        model.addAttribute("tagIdsSelecionadas", tagIdsSelecionadas);

        return "evento/editar";
    }

    // ── POST: salva edição do evento ──────────────────────────────────────────

    @PostMapping("/eventos/editar/{id}")
    public String editarEvento(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("tipoEvento") TipoEvento tipoEvento,
            @RequestParam("modalidade") ModalidadeEvento modalidade,
            @RequestParam("dataInicioData") String dataInicioData,
            @RequestParam("dataInicioHora") String dataInicioHora,
            @RequestParam("dataFimData") String dataFimData,
            @RequestParam("dataFimHora") String dataFimHora,
            @RequestParam(value = "local", required = false) String local,
            @RequestParam(value = "vagasMaximas", defaultValue = "0") Integer vagasMaximas,
            @RequestParam(value = "pontosParticipacao", defaultValue = "1") Integer pontosParticipacao,
            @RequestParam(value = "semestreReferencia", required = false) String semestreReferencia,
            @RequestParam(value = "disciplinaRelacionada", required = false) String disciplinaRelacionada,
            @RequestParam(value = "eForum", required = false) String eForumParam,
            @RequestParam(value = "concedeMedalhaEspecial", required = false) String medalhaParam,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "palestranteIds", required = false) List<Long> palestranteIds,
            @RequestParam(value = "patrocinadorIds", required = false) List<Long> patrocinadorIds,
            Model model) {

        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento == null) {
            model.addAttribute("mensagem", "Erro: Evento não encontrado.");
            return "evento/editar";
        }

        try {
            LocalDateTime dataInicio = LocalDateTime.of(
                    java.time.LocalDate.parse(dataInicioData),
                    java.time.LocalTime.parse(dataInicioHora)
            );
            LocalDateTime dataFim = LocalDateTime.of(
                    java.time.LocalDate.parse(dataFimData),
                    java.time.LocalTime.parse(dataFimHora)
            );

            if (!dataFim.isAfter(dataInicio)) {
                model.addAttribute("mensagem", "Erro: a data/hora de fim deve ser posterior à de início.");
                model.addAttribute("evento", evento);
                return "evento/editar";
            }

            evento.setTitulo(titulo);
            evento.setDescricao(descricao);
            evento.setTipoEvento(tipoEvento);
            evento.setModalidade(modalidade);
            evento.setDataInicio(dataInicio);
            evento.setDataFim(dataFim);
            evento.setLocal(local);
            evento.setVagasMaximas(vagasMaximas != null ? vagasMaximas : 0);
            evento.setPontosParticipacao(pontosParticipacao != null ? pontosParticipacao : 1);
            evento.setSemestreReferencia(semestreReferencia);
            evento.setDisciplinaRelacionada(disciplinaRelacionada);
            evento.setEForum("true".equals(eForumParam));
            evento.setConcedeMedalhaEspecial("true".equals(medalhaParam));

            eventoRepository.save(evento);

            // Re-associa tags: remove as antigas e adiciona as novas selecionadas
            List<Tag> todasTags = tagRepository.findAll();
            for (Tag t : todasTags) {
                t.getListaTag().remove(evento);
                tagRepository.save(t);
            }
            if (tagIds != null && !tagIds.isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(tagIds);
                for (Tag t : tags) {
                    t.getListaTag().add(evento);
                    tagRepository.save(t);
                }
            }

            // Re-associa palestrantes
            List<Palestrante> todosPalestrantes = palestranteRepository.findAll();
            for (Palestrante p : todosPalestrantes) {
                p.getListaEvento().remove(evento);
                palestranteRepository.save(p);
            }
            if (palestranteIds != null && !palestranteIds.isEmpty()) {
                List<Palestrante> palestrantes = palestranteRepository.findAllById(palestranteIds);
                for (Palestrante p : palestrantes) {
                    p.getListaEvento().add(evento);
                    palestranteRepository.save(p);
                }
            }

            // Re-associa patrocinadores
            List<Patrocinador> todosPatrocinadores = patrocinadorRepository.findAll();
            for (Patrocinador p : todosPatrocinadores) {
                p.getEventos().remove(evento);
                patrocinadorRepository.save(p);
            }
            if (patrocinadorIds != null && !patrocinadorIds.isEmpty()) {
                List<Patrocinador> patrocinadores = patrocinadorRepository.findAllById(patrocinadorIds);
                for (Patrocinador p : patrocinadores) {
                    p.getEventos().add(evento);
                    patrocinadorRepository.save(p);
                }
            }

            model.addAttribute("mensagem", "✅ Evento \"" + titulo + "\" atualizado com sucesso!");

        } catch (Exception e) {
            model.addAttribute("mensagem", "Erro ao atualizar evento: " + e.getMessage());
        }

        model.addAttribute("evento", evento);
        List<Long> tagIdsSelecionadas = evento.getListaTag() != null
                ? evento.getListaTag().stream().map(Tag::getId).collect(Collectors.toList())
                : List.of();
        model.addAttribute("tagIdsSelecionadas", tagIdsSelecionadas);
        return "evento/editar";
    }
}
