package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dictionary")
public class DictionaryController {
    private final TranslationRepository translationRepository;
    private final EventStateRepository eventStateRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventTypeRepository eventTypesRepository;
    private final EventTypeCategoryRepository eventTypeCategoryRepository;


    public DictionaryController(TranslationRepository translationRepository,
                                EventStateRepository eventStateRepository,
                                EventStatusRepository eventStatusRepository,
                                EventTypeRepository eventTypesRepository,
                                EventTypeCategoryRepository eventTypeCategoryRepository) {
        this.translationRepository = translationRepository;
        this.eventStateRepository = eventStateRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.eventTypesRepository = eventTypesRepository;
        this.eventTypeCategoryRepository = eventTypeCategoryRepository;
    }

    @GetMapping("/states")
    @Operation(summary = "Get event states ids to i18n codes map")
    public List<EventStateDto> getEventStates() {
        return eventStateRepository.findAll().stream().map(eventStateEntity -> new EventStateDto(
                eventStateEntity.getId(),
                eventStateEntity.getCode(),
                TranslationHelper.getTranslationsByCode(translationRepository, eventStateEntity.getCode())
        )).toList();
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get event statuses ids to i18n codes map")
    public List<EventStatusDto> getEventStatuses() {
        return eventStatusRepository.findAll().stream().map(eventStatusEntity -> new EventStatusDto(
                eventStatusEntity.getId(),
                eventStatusEntity.getCode(),
                TranslationHelper.getTranslationsByCode(translationRepository, eventStatusEntity.getCode())
        )).toList();
    }

    @GetMapping("/types")
    @Operation(summary = "Get event type ids to i18n codes map (name and description)")
    public List<EventTypeDto> getEventTypes() {
        return eventTypesRepository.findAll().stream().map(eventTypeEntity -> new EventTypeDto(
                eventTypeEntity.getId(),
                eventTypeEntity.getCategoryId(),
                eventTypeEntity.getNameCode(),
                TranslationHelper.getTranslationsByCode(translationRepository, eventTypeEntity.getNameCode()),
                eventTypeEntity.getDescCode(),
                TranslationHelper.getTranslationsByCode(translationRepository, eventTypeEntity.getDescCode()),
                eventTypeEntity.getDefaultDuration(),
                eventTypeEntity.getDefaultRepeatInterval(),
                eventTypeEntity.getDefaultCost()
        )).toList();
    }


    @GetMapping("/categories")
    @Operation(summary = "Get event type categories ids to i18n codes map")
    public List<EventTypeCategoryDto> getEventCategories() {
        return eventTypeCategoryRepository.findAll().stream().map(eventTypeCategoryEntity -> new EventTypeCategoryDto(
                eventTypeCategoryEntity.getId(),
                eventTypeCategoryEntity.getCode(),
                eventTypeCategoryEntity.getColor(),
                TranslationHelper.getTranslationsByCode(translationRepository, eventTypeCategoryEntity.getCode())
        )).toList();
    }

    public record EventTypeDto(long id,
                               long categoryId,
                               String nameCode,
                               List<TranslationDto> nameTranslations,
                               String descCode,
                               List<TranslationDto> descTranslations,
                               int defaultDuration,
                               int defaultRepeatInterval,
                               double defaultCost) {
    }


    public record EventTypeCategoryDto(long id,
                                       String code,
                                       String color,
                                       List<TranslationDto> translations) {
    }


    public record EventStatusDto(long id,
                                 String code,
                                 List<TranslationDto> translations) {
    }


    public record EventStateDto(long id,
                                String code,
                                List<TranslationDto> translations) {
    }

}
