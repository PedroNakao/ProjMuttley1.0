package com.trabalho.tag.repository;

import com.trabalho.tag.model.Participante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
	
	// Caso precisa procurar um participante por CPF
	Optional<Participante> findByCpf(String cpf);

}