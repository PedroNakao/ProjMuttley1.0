package com.trabalho.tag.model;

public enum ModalidadeEvento {
    PRESENCIAL("Presencial"),
    REMOTO("Remoto"),
    HIBRIDO("Híbrido");

    private final String descricao;

    ModalidadeEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
