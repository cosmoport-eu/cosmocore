package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.TranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Deprecated
public interface TranslationRepository extends JpaRepository<TranslationEntity, Integer> {
    List<TranslationEntity> findAllByLocaleId(long localeId);
}
