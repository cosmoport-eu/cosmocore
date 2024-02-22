package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.EventTypeEntity;
import com.cosmoport.cosmocore.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/t_events")
public class TimeEventsEndpoint {
    private static final String CODE_NAME_PREFIX = "event_type_name_";
    private static final String CODE_DESC_PREFIX = "event_type_desc_";
    private final EventTypeRepository eventTypeRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventStateRepository eventStateRepository;
    private final TranslationRepository translationRepository;
    private final LocaleRepository localeRepository;
    private final ApplicationEventPublisher eventBus;

    public TimeEventsEndpoint(EventTypeRepository eventTypeRepository,
                              EventStatusRepository eventStatusRepository,
                              EventStateRepository eventStateRepository,
                              TranslationRepository translationRepository,
                              LocaleRepository localeRepository,
                              ApplicationEventPublisher eventBus) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.eventStateRepository = eventStateRepository;
        this.translationRepository = translationRepository;
        this.localeRepository = localeRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    @PostMapping("/type")
    public ResultDto create(@RequestBody CreateEventTypeDto dto) {
        final EventTypeEntity newEntity = eventTypeRepository.save(typeFromDto(dto));
        createTranslationsForType(newEntity, dto.name(), dto.description());

        if (dto.subTypes() != null) {
            dto.subTypes().forEach(subType -> {
                final EventTypeEntity subEntity = eventTypeRepository.save(typeFromDto(dto));
                subEntity.setParentId(newEntity.getId());
                createTranslationsForType(subEntity, subType.name(), subType.description());
            });
        }

        return ResultDto.ok();
    }

    @Transactional
    @PutMapping("/type/{id}")
    public ResultDto updateType(@RequestBody UpdateEventTypeDto dto, @PathVariable int id) {
        eventTypeRepository.findById(id).ifPresentOrElse(e -> {
            e.setCategoryId(dto.categoryId());
            e.setDefaultDuration(dto.defaultDuration());
            e.setDefaultCost(dto.defaultCost());
            e.setDefaultRepeatInterval(dto.defaultRepeatInterval());
            e.setParentId(dto.parentId());
            eventTypeRepository.save(e);
            eventBus.publishEvent(new ReloadMessage(this));
        }, () -> {
            throw new IllegalArgumentException();
        });

        return ResultDto.ok();
    }

    @GetMapping("/types")
    public List<EventTypeDto> getEventTypes() {
        return eventTypeRepository.findAll().stream().map(eventTypeEntity -> new EventTypeDto(
                eventTypeEntity.getId(),
                eventTypeEntity.getCategoryId(),
                eventTypeEntity.getNameCode(),
                eventTypeEntity.getDescCode(),
                eventTypeEntity.getDefaultDuration(),
                eventTypeEntity.getDefaultRepeatInterval(),
                eventTypeEntity.getDefaultCost(),
                eventTypeEntity.getParentId()
        )).toList();
    }


    @DeleteMapping("/type/{id}")
    public String delete(@PathVariable("id") final int id) {
        eventTypeRepository.deleteById(id);
        eventBus.publishEvent(new ReloadMessage(this));
        return "{\"deleted\": " + true + '}';
    }

    @GetMapping("/statuses")
    public List<EventStatusDto> getEventStatuses() {
        return eventStatusRepository.findAll().stream().map(eventStatusEntity -> new EventStatusDto(
                eventStatusEntity.getId(),
                eventStatusEntity.getCode()
        )).toList();
    }

    @DeleteMapping("/status/{id}")
    public ResultDto deleteStatus(@PathVariable("id") final int id) {
        eventStatusRepository.findById(id).ifPresentOrElse(state -> {
                    eventStatusRepository.deleteById(id);
                    eventBus.publishEvent(new ReloadMessage(this));
                },
                () -> {
                    throw new IllegalArgumentException();
                });
        return ResultDto.ok();
    }

    @GetMapping("/states")
    public List<EventStateDto> getEventStates() {
        return eventStateRepository.findAll().stream().map(eventStateEntity -> new EventStateDto(
                eventStateEntity.getId(),
                eventStateEntity.getCode()
        )).toList();
    }

    @DeleteMapping("/state/{id}")
    public ResultDto deleteState(@PathVariable("id") final int id) {
        eventStateRepository.findById(id).ifPresentOrElse(state -> {
                    eventStateRepository.deleteById(id);
                    eventBus.publishEvent(new ReloadMessage(this));
                },
                () -> {
                    throw new IllegalArgumentException();
                });
        return ResultDto.ok();
    }

    public record UpdateEventTypeDto(
            int categoryId,
            int defaultDuration,
            int defaultRepeatInterval,
            double defaultCost,
            Integer parentId) {
    }

    public record CreateEventTypeDto(
            int categoryId,
            String name,
            String description,
            int defaultDuration,
            int defaultRepeatInterval,
            double defaultCost,
            List<SubType> subTypes) {
    }

    public record SubType(String name, String description) {
    }

    public record EventTypeDto(int id,
                               int categoryId,
                               String nameCode,
                               String descCode,
                               int defaultDuration,
                               int defaultRepeatInterval,
                               double defaultCost,
                               Integer parentId) {
    }


    public record EventStatusDto(long id, String code) {
    }


    public record EventStateDto(long id, String code) {
    }

    private void createTranslationsForType(final EventTypeEntity newEntity, String name, String desc) {
        if (eventTypeRepository.existsByDescCode(CODE_NAME_PREFIX + newEntity.getId()) ||
                eventTypeRepository.existsByNameCode(CODE_DESC_PREFIX + newEntity.getId())) {
            throw new IllegalStateException();
        }
        newEntity.setNameCode(CODE_NAME_PREFIX + newEntity.getId());
        newEntity.setDescCode(CODE_DESC_PREFIX + newEntity.getId());

        eventTypeRepository.save(newEntity);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, newEntity.getNameCode(), name)
        );

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, newEntity.getDescCode(), desc)
        );
    }

    private EventTypeEntity typeFromDto(CreateEventTypeDto dto) {
        final EventTypeEntity entity = new EventTypeEntity();
        entity.setCategoryId(dto.categoryId());
        entity.setDefaultDuration(dto.defaultDuration());
        entity.setDefaultRepeatInterval(dto.defaultRepeatInterval());
        entity.setDefaultCost(dto.defaultCost());
        entity.setDescCode(UUID.randomUUID().toString());
        entity.setNameCode(UUID.randomUUID().toString());
        return entity;
    }
}
