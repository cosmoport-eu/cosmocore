package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.Constants;
import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import com.cosmoport.cosmocore.controller.dto.EventDtoResponse;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.error.ApiAuthError;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.TimetableEntity;
import com.cosmoport.cosmocore.repository.SettingsRepository;
import com.cosmoport.cosmocore.repository.TimeTableRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class SyncEndpoint {

    private final ApplicationEventPublisher eventBus;
    private final SettingsRepository settingsRepository;
    private final TimeTableRepository timeTableRepository;

    public SyncEndpoint(ApplicationEventPublisher eventBus,
                        SettingsRepository settingsRepository,
                        TimeTableRepository timeTableRepository) {
        this.eventBus = eventBus;
        this.settingsRepository = settingsRepository;
        this.timeTableRepository = timeTableRepository;
    }

    @PostMapping("/tickets")
    public ResultDto syncTickets(final SyncTicketsDto syncTickets) {
        auth(syncTickets);

        timeTableRepository.findById(syncTickets.eventId()).ifPresentOrElse(
                timeTableEntity -> {
                    timeTableEntity.setContestants(syncTickets.value());
                    timeTableRepository.save(timeTableEntity);
                },
                () -> {
                    throw new RuntimeException("Event not found");
                }
        );

        return new ResultDto(true);
    }

    @PostMapping("/add/event")
    public EventDtoResponse create(final SyncAddEventDto syncAddEvent) {
        auth(syncAddEvent);
        final TimetableEntity timetableEntity = timeTableRepository.findById(syncAddEvent.event().id()).orElse(
                new TimetableEntity(syncAddEvent.event().id(),
                        syncAddEvent.event().eventDate(),
                        syncAddEvent.event().eventTypeId(),
                        syncAddEvent.event().eventStatusId(),
                        syncAddEvent.event().eventStateId(),
                        syncAddEvent.event().gateId(),
                        syncAddEvent.event().gate2Id(),
                        syncAddEvent.event().startTime(),
                        syncAddEvent.event().durationTime(),
                        syncAddEvent.event().repeatInterval(),
                        syncAddEvent.event().cost(),
                        syncAddEvent.event().peopleLimit(),
                        syncAddEvent.event().contestants(),
                        syncAddEvent.event().dateAdded()
                )
        );

        timetableEntity.setEventDate(syncAddEvent.event().eventDate());
        timetableEntity.setEventTypeId(syncAddEvent.event().eventTypeId());
        timetableEntity.setEventStateId(syncAddEvent.event().eventStateId());
        timetableEntity.setEventStatusId(syncAddEvent.event().eventStatusId());
        timetableEntity.setGateId(syncAddEvent.event().gateId());
        timetableEntity.setGate2Id(syncAddEvent.event().gate2Id());
        timetableEntity.setStartTime(syncAddEvent.event().startTime());
        timetableEntity.setDurationTime(syncAddEvent.event().durationTime());
        timetableEntity.setRepeatInterval(syncAddEvent.event().repeatInterval());
        timetableEntity.setCost(syncAddEvent.event().cost());
        timetableEntity.setPeopleLimit(syncAddEvent.event().peopleLimit());
        timetableEntity.setContestants(syncAddEvent.event().contestants());

        TimetableEntity updatedEntity = timeTableRepository.save(timetableEntity);

        eventBus.publishEvent(new ReloadMessage(this));

        return new EventDtoResponse(
                updatedEntity.getId(),
                updatedEntity.getEventDate(),
                updatedEntity.getEventTypeId(),
                updatedEntity.getEventStateId(),
                updatedEntity.getEventStatusId(),
                updatedEntity.getGateId(),
                updatedEntity.getGate2Id(),
                updatedEntity.getStartTime(),
                updatedEntity.getDurationTime(),
                updatedEntity.getRepeatInterval(),
                updatedEntity.getCost(),
                updatedEntity.getPeopleLimit(),
                updatedEntity.getContestants(),
                updatedEntity.getDateAdded()
        );
    }

    private void auth(HasAuthKey syncRequest) {
        final boolean isKeyOk = settingsRepository.findByParam(Constants.SYNC_SERVER_KEY).orElseThrow()
                .getValue().equals(syncRequest.key());

        if (!isKeyOk) {
            throw new ApiAuthError();
        }
    }


    public interface HasAuthKey {
        String key();
    }


    public record SyncTicketsDto(String key, int eventId, int value, String timestamp) implements HasAuthKey {
        @JsonCreator
        public SyncTicketsDto(@JsonProperty("key") String key,
                              @JsonProperty("event_id") int eventId,
                              @JsonProperty("value") int value,
                              @JsonProperty("timestamp") String timestamp) {
            this.key = key;
            this.eventId = eventId;
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    public record SyncAddEventDto(String key,
                                  EventDtoRequest event,
                                  String timestamp) implements HasAuthKey {
        @Override
        public String key() {
            return null;
        }
    }

}
