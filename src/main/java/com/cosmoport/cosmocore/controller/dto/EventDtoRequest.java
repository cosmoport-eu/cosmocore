package com.cosmoport.cosmocore.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//FIXME нужно убрать snake_case в названиях полей и заменив на camelCase оставить только один из двух EventDto
public record EventDtoRequest(
        @JsonProperty("id") int id,
        @JsonProperty("event_date") String eventDate,
        @JsonProperty("event_type_id") int eventTypeId,
        @JsonProperty("event_state_id") int eventStateId,
        @JsonProperty("event_status_id") int eventStatusId,
        @JsonProperty("gate_id") int gateId,
        @JsonProperty("gate2_id") int gate2Id,
        @JsonProperty("start_time") int startTime,
        @JsonProperty("duration_time") int durationTime,
        @JsonProperty("repeat_interval") int repeatInterval,
        @JsonProperty("cost") double cost,
        @JsonProperty("people_limit") int peopleLimit,
        @JsonProperty("contestants") int contestants,
        @JsonProperty("date_added") String dateAdded
) {
}