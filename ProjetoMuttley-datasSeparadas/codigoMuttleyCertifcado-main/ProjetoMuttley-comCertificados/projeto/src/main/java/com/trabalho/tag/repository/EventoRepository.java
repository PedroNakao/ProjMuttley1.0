package com.trabalho.tag.repository;

import com.trabalho.tag.model.Evento;
import com.trabalho.tag.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Busca todos os eventos ativos
    List<Evento> findByAtivoTrue();

    // Busca eventos por tipo
    List<Evento> findByTipoEventoAndAtivoTrue(TipoEvento tipoEvento);

    // Busca eventos de um semestre específico
    List<Evento> findBySemestreReferenciaAndAtivoTrue(String semestreReferencia);

    // Busca eventos que são fórum
    List<Evento> findByeForumTrueAndAtivoTrue();

    // Busca eventos por disciplina
    List<Evento> findByDisciplinaRelacionadaContainingIgnoreCaseAndAtivoTrue(String disciplina);

    // Busca eventos dentro de um período
    @Query("SELECT e FROM Evento e WHERE e.ativo = true AND e.dataInicio BETWEEN :inicio AND :fim")
    List<Evento> findEventosNoPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Busca eventos por tag de competência
    @Query("SELECT e FROM Evento e JOIN e.listaTag t WHERE t.id = :tagId AND e.ativo = true")
    List<Evento> findEventosByTagId(@Param("tagId") Long tagId);
}
