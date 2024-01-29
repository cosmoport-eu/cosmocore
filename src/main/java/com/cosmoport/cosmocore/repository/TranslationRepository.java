package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.TranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TranslationRepository extends JpaRepository<TranslationEntity, Integer> {
    List<TranslationEntity> findAllByLocaleId(long localeId);
    Optional<TranslationEntity> findByLocaleIdAndCode(long localeId, String code);

    void deleteAllByCode(String code);

    List<TranslationEntity> findAllByCode(String code);

    default Map<Integer, String> getTranslationsMap(final String code) {
        return this.findAllByCode(code).stream()
                .collect(Collectors.toMap(TranslationEntity::getLocaleId, TranslationEntity::getText));
    }
}
