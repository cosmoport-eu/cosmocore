package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import com.cosmoport.cosmocore.controller.dto.EventDtoResponse;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.error.ValidationException;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.events.SyncTimetablesMessage;
import com.cosmoport.cosmocore.model.EventTypeCategoryEntity;
import com.cosmoport.cosmocore.model.EventTypeEntity;
import com.cosmoport.cosmocore.model.TimetableEntity;
import com.cosmoport.cosmocore.repository.EventTypeCategoryRepository;
import com.cosmoport.cosmocore.repository.EventTypeRepository;
import com.cosmoport.cosmocore.repository.TimeTableRepository;
import com.cosmoport.cosmocore.service.RemoteSync;
import com.cosmoport.cosmocore.service.Types;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timetable")
public class TimetableEndpoint {
    private final ApplicationEventPublisher eventBus;
    private final RemoteSync remoteSync;
    private final TimeTableRepository timeTableRepository;
    private final EventTypeRepository eventTypeRepository;
    private final EventTypeCategoryRepository eventTypeCategoryRepository;

    public TimetableEndpoint(ApplicationEventPublisher eventBus,
                             RemoteSync remoteSync,
                             TimeTableRepository timeTableRepository,
                             EventTypeRepository eventTypeRepository,
                             EventTypeCategoryRepository eventTypeCategoryRepository) {
        this.eventBus = eventBus;
        this.remoteSync = remoteSync;
        this.timeTableRepository = timeTableRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.eventTypeCategoryRepository = eventTypeCategoryRepository;
    }

    @GetMapping("/all")
    public List<EventDtoResponse> getAll(
            @RequestParam int page,
            @RequestParam int count) {
        final List<EventDtoResponse> events =
                timeTableRepository.findAllByEventDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .stream()
                        .skip((page - 1L) * count)
                        .limit(count)
                        .map(this::convertToDto)
                        .toList();

        // We assume that this method will be called on every timetable app opening
        eventBus.publishEvent(new SyncTimetablesMessage(this));
        return events;
    }

    /**
     * Gets the event with {@code id} and one event after that for same gate.
     *
     * @param id long An id of event.
     * @return Two events.
     */
    @GetMapping("/byIdAndOneAfter")
    public List<EventDtoResponse> getEvents(@RequestParam("id") int id) {
        final Optional<TimetableEntity> mainEvent = timeTableRepository.findById(id);
        if (mainEvent.isEmpty()) {
            return Collections.emptyList();
        }

        final TimetableEntity main = mainEvent.get();
        return timeTableRepository.findNextEventForGate(main.getEventDate(), main.getGateId(), main.getGate2Id(), main.getStartTime(), main.getEventStatusId(), main.getId())
                .map(next -> Arrays.asList(convertToDto(main), convertToDto(next)))
                .orElse(Collections.singletonList(convertToDto(main)));
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") final int id) {
        timeTableRepository.deleteById(id);
        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.DELETE, new RemoteSync.EventIdDto(id));
        return "{\"deleted\": true}";
    }

    /**
     * Updates the number of sold tickets. Same as {@link SyncEndpoint::updateTickets}.
     *
     * @param request An object containing an events' id and its new tickets count value.
     * @return A result object.
     * @throws RuntimeException In case of any errors.
     */
    @PostMapping("/tickets")
    public ResultDto updateTickets(@RequestBody TicketsUpdateRequestDto request) {
        final Boolean result = timeTableRepository.findById(request.id()).map(
                        timetableEntity -> {
                            timetableEntity.setContestants(request.tickets());
                            timetableEntity.setEventStateId(1);
                            timeTableRepository.save(timetableEntity);
                            return true;
                        })
                .orElse(false);
        return new ResultDto(result);
    }

    @GetMapping("/suggest/next")
    public TimeSuggestionDto getSuggestion(@PathVariable("gate") int gateId,
                                           @PathVariable("date") final String date) {
        if (gateId <= 0) {
            throw new ValidationException("Set the gate number.");
        }

        final String dateString = date != null && !date.isEmpty() ? date :
                new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return new TimeSuggestionDto(timeTableRepository.getLastTimeForGate(gateId, dateString));
    }

    @PostMapping("/update/event")
    public EventDtoResponse update(@RequestBody EventDtoRequest event) {
        final TimetableEntity toUpdate = timeTableRepository.findById(event.id()).orElseThrow();

        toUpdate.setEventDate(event.eventDate());
        toUpdate.setEventTypeId(event.eventTypeId());
        toUpdate.setEventStateId(event.eventStateId());
        toUpdate.setEventStatusId(event.eventStatusId());
        toUpdate.setGateId(event.gateId());
        toUpdate.setGate2Id(event.gate2Id());
        toUpdate.setStartTime(event.startTime());
        toUpdate.setDurationTime(event.durationTime());
        toUpdate.setRepeatInterval(event.repeatInterval());
        toUpdate.setCost(event.cost());
        toUpdate.setPeopleLimit(event.peopleLimit());
        toUpdate.setContestants(event.contestants());
        toUpdate.setDateAdded(event.dateAdded());

        EventDtoResponse newEvent = convertToDto(timeTableRepository.save(toUpdate));

        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.UPDATE, new RemoteSync.EventIdDto(event.id()));

        return newEvent;
    }


    @PostMapping
    public EventDtoResponse create(@RequestBody final EventDtoRequest event) {
        final TimetableEntity timetableEntity = timeTableRepository.save(new TimetableEntity(
                event.id(),
                event.eventDate(),
                event.eventTypeId(),
                event.eventStatusId(),
                event.eventStateId(),
                event.gateId(),
                event.gate2Id(),
                event.startTime(),
                event.durationTime(),
                event.repeatInterval(),
                event.cost(),
                event.peopleLimit(),
                event.contestants(),
                event.dateAdded()));


        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.CREATE, new RemoteSync.EventIdDto(timetableEntity.getId()));
        return new EventDtoResponse(
                timetableEntity.getId(),
                timetableEntity.getEventDate(),
                timetableEntity.getEventTypeId(),
                timetableEntity.getEventStateId(),
                timetableEntity.getEventStatusId(),
                timetableEntity.getGateId(),
                timetableEntity.getGate2Id(),
                timetableEntity.getStartTime(),
                timetableEntity.getDurationTime(),
                timetableEntity.getRepeatInterval(),
                timetableEntity.getCost(),
                timetableEntity.getPeopleLimit(),
                timetableEntity.getContestants(),
                timetableEntity.getDateAdded());
    }

    //FIXME: нужно сделать отдельные запросы для поиска по gate-у и датам
    @GetMapping
    public List<EventDtoWithColor> get(@RequestParam(required = false) String date, @RequestParam(required = false) String date2,
                                       @RequestParam(required = false) Integer gateId) {
        final Map<Integer, String> categoryTypeToColorMap = eventTypeCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeCategoryEntity::getId, EventTypeCategoryEntity::getColor));
        final Map<Integer, String> categoryIdToColorMap = eventTypeRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeEntity::getId, ete -> categoryTypeToColorMap.get(ete.getCategoryId())));


        return timeTableRepository.findAll().stream()
                .filter(event -> {
                    if (date == null) {
                        return true;
                    } else {
                        return event.getEventDate().compareTo(date) >= 0;
                    }
                })
                .filter(event -> {
                    if (date2 == null) {
                        return event.getGateId().equals(gateId);
                    } else {
                        return event.getEventDate().compareTo(date2) <= 0;
                    }
                })
                .sorted(Comparator.comparing(TimetableEntity::getEventDate).thenComparing(TimetableEntity::getGateId))
                .map(event -> new EventDtoWithColor(
                        event.getId(),
                        event.getEventDate(),
                        event.getEventTypeId(),
                        categoryIdToColorMap.get(event.getEventTypeId()),
                        event.getEventStateId(),
                        event.getEventStatusId(),
                        event.getGateId(),
                        event.getGate2Id(),
                        event.getStartTime(),
                        event.getDurationTime(),
                        event.getRepeatInterval(),
                        event.getCost(),
                        event.getPeopleLimit(),
                        event.getContestants(),
                        event.getDateAdded()))
                .toList();
    }


    public record EventDtoWithColor(int id, String eventDate, int eventTypeId, String eventColor, int eventStateId,
                                    int eventStatusId, int gateId, int gate2Id, int startTime, int durationTime,
                                    int repeatInterval,
                                    double cost, int peopleLimit, int contestants, String dateAdded
    ) {
    }


    private EventDtoResponse convertToDto(TimetableEntity timetableEntity) {
        return new EventDtoResponse(
                timetableEntity.getId(),
                timetableEntity.getEventDate(),
                timetableEntity.getEventTypeId(),
                timetableEntity.getEventStateId(),
                timetableEntity.getEventStatusId(),
                timetableEntity.getGateId(),
                timetableEntity.getGate2Id(),
                timetableEntity.getStartTime(),
                timetableEntity.getDurationTime(),
                timetableEntity.getRepeatInterval(),
                timetableEntity.getCost(),
                timetableEntity.getPeopleLimit(),
                timetableEntity.getContestants(),
                timetableEntity.getDateAdded());
    }

    public record TicketsUpdateRequestDto(int id, int tickets, boolean forceOpen) {
        @JsonCreator
        public TicketsUpdateRequestDto(@JsonProperty("id") int id,
                                       @JsonProperty("tickets") int tickets,
                                       @JsonProperty("forceOpen") boolean forceOpen) {
            this.id = id;
            this.tickets = tickets;
            this.forceOpen = forceOpen;
        }
    }

    public record TimeSuggestionDto(int time) {
    }

}
