package com.trabalho.tag.dto;

import com.trabalho.tag.model.ModalidadeEvento;
import com.trabalho.tag.model.TipoEvento;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventoRequestDTO {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "Tipo do evento é obrigatório")
    private TipoEvento tipoEvento;

    @NotNull(message = "Modalidade é obrigatória")
    private ModalidadeEvento modalidade;

    @NotNull(message = "Data de início é obrigatória")
    @FutureOrPresent(message = "Data de início não pode ser no passado")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDateTime dataFim;

    @Size(max = 300, message = "Local deve ter no máximo 300 caracteres")
    private String local;

    @Min(value = 0, message = "Vagas máximas não pode ser negativo")
    private Integer vagasMaximas = 0;

    @Size(max = 10, message = "Semestre deve ter no máximo 10 caracteres")
    private String semestreReferencia;

    @Size(max = 150, message = "Disciplina deve ter no máximo 150 caracteres")
    private String disciplinaRelacionada;

    private Boolean eForum = false;

    private Boolean concedeMedalhaEspecial = false;

    @Min(value = 0, message = "Pontos não pode ser negativo")
    private Integer pontosParticipacao = 1;

    // IDs das tags selecionadas no dropdown (pode ser mais de uma)
    private List<Long> tagIds;

    // IDs dos tipos de certificado selecionados no dropdown (pode ser mais de um)
    private List<Long> tipoCertificadoIds;

    // IDs dos palestrantes (suporte a múltiplos palestrantes)
    private List<Long> palestranteIds;

    // IDs dos patrocinadores (suporte a múltiplos patrocinadores)
    private List<Long> patrocinadorIds;
}
