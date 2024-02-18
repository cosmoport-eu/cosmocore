package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.repository.EventStateRepository;
import com.cosmoport.cosmocore.repository.EventStatusRepository;
import com.cosmoport.cosmocore.repository.EventTypeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/t_events")
public class TimeEventsEndpoint {
    private final EventTypeRepository eventTypeRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventStateRepository eventStateRepository;
    private final ApplicationEventPublisher eventBus;

    public TimeEventsEndpoint(EventTypeRepository eventTypeRepository,
                              EventStatusRepository eventStatusRepository,
                              EventStateRepository eventStateRepository,
                              ApplicationEventPublisher eventBus) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.eventStateRepository = eventStateRepository;
        this.eventBus = eventBus;
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

    public record EventReferenceDataDto(List<EventTypeDto> types,
                                        List<EventTypeCategoryDto> typeCategories,
                                        List<EventStatusDto> statuses,
                                        List<EventStateDto> states) {
    }

    public record EventTypeDto(long id,
                               long categoryId,
                               String nameCode,
                               String descCode,
                               int defaultDuration,
                               int defaultRepeatInterval,
                               double defaultCost) {
    }


    public record EventTypeCategoryDto(long id,
                                       String code,
                                       long parent,
                                       String color) {
    }


    public record EventStatusDto(long id,
                                 String code) {
    }


    public record EventStateDto(long id,
                                String code) {
    }

}
