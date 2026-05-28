package com.trabalho.tag.repository;

import com.trabalho.tag.model.Palestrante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PalestranteRepository extends JpaRepository<Palestrante, Long> {

    List<Palestrante> findByAtivoTrueOrderByNomeAsc();

    List<Palestrante> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
}
