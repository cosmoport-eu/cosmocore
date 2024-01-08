package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.repository.EventStateRepository;
import com.cosmoport.cosmocore.repository.EventStatusRepository;
import com.cosmoport.cosmocore.repository.EventTypeCategoryRepository;
import com.cosmoport.cosmocore.repository.EventTypeRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/t_events")
public class TimeEventsEndpoint {
    private final EventTypeRepository eventTypeRepository;
    private final EventTypeCategoryRepository eventTypeCategoryRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventStateRepository eventStateRepository;
    private final ApplicationEventPublisher eventBus;

    public TimeEventsEndpoint(EventTypeRepository eventTypeRepository,
                              EventTypeCategoryRepository eventTypeCategoryRepository,
                              EventStatusRepository eventStatusRepository,
                              EventStateRepository eventStateRepository,
                              ApplicationEventPublisher eventBus) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventTypeCategoryRepository = eventTypeCategoryRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.eventStateRepository = eventStateRepository;
        this.eventBus = eventBus;
    }

    @GetMapping("/reference_data")
    public EventReferenceDataDto getEventReferenceData() {
        return new EventReferenceDataDto(
                eventTypeRepository.findAll().stream().map(eventTypeEntity -> new EventTypeDto(
                        eventTypeEntity.getId(),
                        eventTypeEntity.getCategoryId(),
                        eventTypeEntity.getI18NEventTypeName(),
                        eventTypeEntity.getI18NEventTypeDescription(),
                        eventTypeEntity.getDefaultDuration(),
                        eventTypeEntity.getDefaultRepeatInterval(),
                        eventTypeEntity.getDefaultCost()
                )).toList(),
                eventTypeCategoryRepository.findAll().stream().map(eventTypeCategoryEntity -> new EventTypeCategoryDto(
                        eventTypeCategoryEntity.getId(),
                        eventTypeCategoryEntity.getI18NEventTypeCategoryName(),
                        eventTypeCategoryEntity.getParent() == null ? 0 : eventTypeCategoryEntity.getParent(),
                        eventTypeCategoryEntity.getColor()
                )).toList(),
                eventStatusRepository.findAll().stream().map(eventStatusEntity -> new EventStatusDto(
                        eventStatusEntity.getId(),
                        eventStatusEntity.getI18NStatus()
                )).toList(),
                eventStateRepository.findAll().stream().map(eventStateEntity -> new EventStateDto(
                        eventStateEntity.getId(),
                        eventStateEntity.getI18NState()
                )).toList());
    }


    @GetMapping("/types")
    public List<EventTypeDto> getEventTypes() {
        return eventTypeRepository.findAll().stream().map(eventTypeEntity -> new EventTypeDto(
                eventTypeEntity.getId(),
                eventTypeEntity.getCategoryId(),
                eventTypeEntity.getI18NEventTypeName(),
                eventTypeEntity.getI18NEventTypeDescription(),
                eventTypeEntity.getDefaultDuration(),
                eventTypeEntity.getDefaultRepeatInterval(),
                eventTypeEntity.getDefaultCost()
        )).toList();
    }


    @DeleteMapping("/types/{id}")
    public String delete(@PathVariable("id") final int id) {
        eventTypeRepository.deleteById(id);
        eventBus.publishEvent(new ReloadMessage(this));
        return "{\"deleted\": " + true + '}';
    }

    @GetMapping("/statuses")
    public List<EventStatusDto> getEventStatuses() {
        return eventStatusRepository.findAll().stream().map(eventStatusEntity -> new EventStatusDto(
                eventStatusEntity.getId(),
                eventStatusEntity.getI18NStatus()
        )).toList();
    }

    @GetMapping("/states")
    public List<EventStateDto> getEventStates() {
        return eventStateRepository.findAll().stream().map(eventStateEntity -> new EventStateDto(
                eventStateEntity.getId(),
                eventStateEntity.getI18NState()
        )).toList();
    }

    public record EventReferenceDataDto(List<EventTypeDto> types,
                                        @JsonProperty("type_categories") List<EventTypeCategoryDto> typeCategories,
                                        List<EventStatusDto> statuses,
                                        List<EventStateDto> states) {
    }

    public record EventTypeDto(long id,
                               long categoryId,
                               long i18nEventTypeName,
                               long i18nEventTypeDescription,
                               int defaultDuration,
                               int defaultRepeatInterval,
                               double defaultCost) {
    }


    public record EventTypeCategoryDto(long id,
                                       long i18nEventTypeCategoryName,
                                       long parent,
                                       String color) {
    }


    public record EventStatusDto(long id,
                                 long i18nStatus) {
    }


    public record EventStateDto(long id,
                                long i18nState) {
    }


    public record CreateEventTypeRequestDto(int categoryId,
                                            String name,
                                            String description,
                                            List<CreateEventSubTypeRequestDto> subtypes,
                                            int defaultDuration,
                                            int defaultRepeatInterval,
                                            double defaultCost) {
        @JsonCreator
        public CreateEventTypeRequestDto(@JsonProperty("category_id") int categoryId,
                                         @JsonProperty("name") String name,
                                         @JsonProperty("description") String description,
                                         @JsonProperty("subtypes") List<CreateEventSubTypeRequestDto> subtypes,
                                         @JsonProperty("default_duration") int defaultDuration,
                                         @JsonProperty("default_repeat_interval") int defaultRepeatInterval,
                                         @JsonProperty("default_cost") double defaultCost) {
            this.categoryId = categoryId;
            // trim
            this.name = (name != null) ? name.trim() : null;
            this.description = description;
            this.subtypes = subtypes;
            this.defaultDuration = defaultDuration;
            this.defaultRepeatInterval = defaultRepeatInterval;
            this.defaultCost = defaultCost;
        }
    }

    public record CreateEventSubTypeRequestDto(String name, String description) {
        @JsonCreator
        public CreateEventSubTypeRequestDto(@JsonProperty("name") String name, @JsonProperty("description") String description) {
            this.name = name;
            this.description = description;
        }
    }

    public record EventTypeSaveResultDto(List<EventTypeCategoryDto> eventTypeCategories,
                                         List<EventTypeDto> eventTypes) {
        @JsonCreator
        public EventTypeSaveResultDto(
                @JsonProperty("event_type_categories") List<EventTypeCategoryDto> eventTypeCategories,
                @JsonProperty("event_types") List<EventTypeDto> eventTypes) {
            this.eventTypeCategories = eventTypeCategories;
            this.eventTypes = eventTypes;
        }
    }


}
