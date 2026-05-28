package com.trabalho.tag.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trabalho.tag.model.Tag;
import com.trabalho.tag.repository.TagRepository;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Tag> listarTodas() {
        return tagRepository.findAll();
    }

    public void salvar(String nome) {
        Tag tag = new Tag();
        tag.setNome(nome);
        tagRepository.save(tag);
    }

    public void editar(Long id, String nome) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag não encontrada"));
        tag.setNome(nome.trim());
        tagRepository.save(tag);
    }

    public void excluir(Long id) {
        tagRepository.deleteById(id);
    }
}
