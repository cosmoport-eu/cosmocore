package com.cosmoport.cosmocore.controller.helper;

import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.repository.TranslationRepository;

import java.util.List;

public final class TranslationHelper {
    private TranslationHelper() {
    }

    public static List<TranslationDto> getTranslationsByCode(TranslationRepository translationRepository,
                                                             String code) {
        return translationRepository.findAllByCode(code).stream()
                .map(entity-> new TranslationDto(entity.getId(), entity.getLocaleId(), entity.getText()))
                .toList();
    }
}
