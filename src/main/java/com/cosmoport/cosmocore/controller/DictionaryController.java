package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.model.EventStateEntity;
import com.cosmoport.cosmocore.model.EventStatusEntity;
import com.cosmoport.cosmocore.model.EventTypeCategoryEntity;
import com.cosmoport.cosmocore.model.EventTypeEntity;
import com.cosmoport.cosmocore.repository.EventStateRepository;
import com.cosmoport.cosmocore.repository.EventStatusRepository;
import com.cosmoport.cosmocore.repository.EventTypeCategoryRepository;
import com.cosmoport.cosmocore.repository.EventTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dictionary")
public class DictionaryController {
    private final EventStateRepository eventStateRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventTypeRepository eventTypesRepository;
    private final EventTypeCategoryRepository eventTypeCategoryRepository;

    public DictionaryController(EventStateRepository eventStateRepository,
                                EventStatusRepository eventStatusRepository,
                                EventTypeRepository eventTypesRepository,
                                EventTypeCategoryRepository eventTypeCategoryRepository) {
        this.eventStateRepository = eventStateRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.eventTypesRepository = eventTypesRepository;
        this.eventTypeCategoryRepository = eventTypeCategoryRepository;
    }

    @GetMapping("/states")
    @Operation(summary = "Get event states ids to i18n codes map")
    public Map<Integer, String> getEventStates() {
        return eventStateRepository.findAll().stream()
                .collect(Collectors.toMap(EventStateEntity::getId, EventStateEntity::getCode));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get event statuses ids to i18n codes map")
    public Map<Integer, String> getEventStatuses() {
        return eventStatusRepository.findAll().stream()
                .collect(Collectors.toMap(EventStatusEntity::getId, EventStatusEntity::getCode));
    }

    @GetMapping("/types")
    @Operation(summary = "Get event type ids to i18n codes map (name and description)")
    public Map<Integer, EventTypeDto> getEventTypes() {
        return eventTypesRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeEntity::getId, e -> new EventTypeDto(e.getNameCode(), e.getDescCode())));
    }


    @GetMapping("/categories")
    @Operation(summary = "Get event type categories ids to i18n codes map")
    public Map<Integer, String> getEventCategories() {
        return eventTypeCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeCategoryEntity::getId, EventTypeCategoryEntity::getCode));
    }

    public record EventTypeDto(String nameCode, String descCode) {
    }

}
