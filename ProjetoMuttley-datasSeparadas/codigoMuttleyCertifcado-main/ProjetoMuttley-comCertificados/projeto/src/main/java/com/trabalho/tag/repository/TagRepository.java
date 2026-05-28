package com.trabalho.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.tag.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
}