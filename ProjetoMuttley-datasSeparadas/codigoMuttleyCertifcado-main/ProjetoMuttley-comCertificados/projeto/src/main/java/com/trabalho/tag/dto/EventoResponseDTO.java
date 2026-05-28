package com.trabalho.tag.dto;

import com.trabalho.tag.model.ModalidadeEvento;
import com.trabalho.tag.model.TipoEvento;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventoResponseDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private TipoEvento tipoEvento;
    private String tipoEventoDescricao;
    private ModalidadeEvento modalidade;
    private String modalidadeDescricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Integer cargaHorariaMinutos;
    private String cargaHorariaFormatada; // "2h 30min"
    private String local;
    private Integer vagasMaximas;
    private String semestreReferencia;
    private String disciplinaRelacionada;
    private Boolean eForum;
    private Boolean concedeMedalhaEspecial;
    private Integer pontosParticipacao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Dados completos dos relacionamentos
    private List<TagSimplesDTO> tags;
    private List<TipoCertificadoSimplesDTO> tiposCertificado;
    private List<PalestranteSimplesDTO> palestrantes;
    private List<PatrocinadorSimplesDTO> patrocinadores;

    // --- DTOs internos para simplificar a resposta ---

    @Data
    public static class TagSimplesDTO {
        private Long id;
        private String nome;
        private String areaConhecimento;
    }

    @Data
    public static class TipoCertificadoSimplesDTO {
        private Long id;
        private String nome;
        private Integer pontosConcedidos;
        private Boolean concedemedalhaEspecial;
    }

    @Data
    public static class PalestranteSimplesDTO {
        private Long id;
        private String nome;
        private String titulo;
        private String linkedinUrl;
        private String githubUrl;
    }

    @Data
    public static class PatrocinadorSimplesDTO {
        private Long id;
        private String nome;
        private String nivelPatrocinio;
        private String site;
    }
}
