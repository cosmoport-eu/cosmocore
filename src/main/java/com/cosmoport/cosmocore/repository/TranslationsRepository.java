package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.TranslationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslationsRepository extends JpaRepository<TranslationsEntity, Integer> {
    List<TranslationsEntity> findAllByLocaleId(int localeId);
}
