package com.trabalho.tag.dto;

import com.trabalho.tag.model.ModalidadeEvento;
import com.trabalho.tag.model.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que agrega todos os dados necessários para popular
 * os dropdowns na tela de cadastro de evento.
 *
 * O frontend chama GET /api/eventos/dropdowns e recebe
 * tudo de uma vez só, evitando múltiplas requisições.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropdownsEventoDTO {

    // Opções do dropdown "Tipo de Evento"
    private List<OpcaoEnumDTO> tiposEvento;

    // Opções do dropdown "Modalidade"
    private List<OpcaoEnumDTO> modalidades;

    // Opções do dropdown/multiselect "Tags de Competência"
    private List<TagOpcaoDTO> tags;

    // Opções do dropdown/multiselect "Tipo de Certificado"
    private List<TipoCertificadoOpcaoDTO> tiposCertificado;

    // Opções do multiselect "Palestrantes"
    private List<PalestranteOpcaoDTO> palestrantes;

    // Opções do multiselect "Patrocinadores"
    private List<PatrocinadorOpcaoDTO> patrocinadores;

    // --- DTOs para cada dropdown ---

    @Data
    @AllArgsConstructor
    public static class OpcaoEnumDTO {
        private String valor;    // ex: "PALESTRA"
        private String label;    // ex: "Palestra"
    }

    @Data
    @AllArgsConstructor
    public static class TagOpcaoDTO {
        private Long id;
        private String nome;
        private String areaConhecimento;
    }

    @Data
    @AllArgsConstructor
    public static class TipoCertificadoOpcaoDTO {
        private Long id;
        private String nome;
        private Integer pontosConcedidos;
        private Boolean concedeMedalhaEspecial;
    }

    @Data
    @AllArgsConstructor
    public static class PalestranteOpcaoDTO {
        private Long id;
        private String nome;
        private String titulo;
    }

    @Data
    @AllArgsConstructor
    public static class PatrocinadorOpcaoDTO {
        private Long id;
        private String nome;
        private String nivelPatrocinio;
    }

    // --- Métodos estáticos utilitários ---

    public static OpcaoEnumDTO fromTipoEvento(TipoEvento tipo) {
        return new OpcaoEnumDTO(tipo.name(), tipo.getDescricao());
    }

    public static OpcaoEnumDTO fromModalidade(ModalidadeEvento modalidade) {
        return new OpcaoEnumDTO(modalidade.name(), modalidade.getDescricao());
    }
}
