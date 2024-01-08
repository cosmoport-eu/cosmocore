package com.cosmoport.cosmocore.events;

import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import org.springframework.context.ApplicationEvent;

public final class FireUpGateMessage extends ApplicationEvent {
    public FireUpGateMessage(Object source) {
        super(source);
    }
}