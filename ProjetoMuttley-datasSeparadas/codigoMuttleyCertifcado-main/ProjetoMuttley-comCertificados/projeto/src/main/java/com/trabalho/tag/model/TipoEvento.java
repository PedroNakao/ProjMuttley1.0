package com.trabalho.tag.model;

public enum TipoEvento {
    AULA_ABERTA("Aula Aberta"),
    PALESTRA("Palestra"),
    WORKSHOP("Workshop"),
    FORUM("Fórum"),
    MARATONA_PROGRAMACAO("Maratona de Programação"),
    PORTAS_ABERTAS("Portas Abertas"),
    MEETUP("Meetup"),
    OUTRO("Outro");

    private final String descricao;

    TipoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
