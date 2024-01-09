package com.cosmoport.cosmocore.controller.dto;

public record EventDtoResponse(int id, String eventDate, int eventTypeId, int eventStateId,
                               int eventStatusId, int gateId, int gate2Id, int startTime, int durationTime,
                               int repeatInterval,  double cost, int peopleLimit, int contestants, String dateAdded
) {
}
