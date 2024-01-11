package com.cosmoport.cosmocore.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//FIXME нужно убрать snake_case в названиях полей и заменив на camelCase оставить только один из двух EventDto
public record EventDtoRequest(
        @JsonProperty("id") int id,
        @JsonProperty("eventDate") String eventDate,
        @JsonProperty("eventTypeId") int eventTypeId,
        @JsonProperty("eventStateId") int eventStateId,
        @JsonProperty("eventStatusId") int eventStatusId,
        @JsonProperty("gateId") int gateId,
        @JsonProperty("gate2Id") int gate2Id,
        @JsonProperty("startTime") int startTime,
        @JsonProperty("durationTime") int durationTime,
        @JsonProperty("repeatInterval") int repeatInterval,
        @JsonProperty("cost") double cost,
        @JsonProperty("peopleLimit") int peopleLimit,
        @JsonProperty("contestants") int contestants,
        @JsonProperty("dateAdded") String dateAdded
) {
}