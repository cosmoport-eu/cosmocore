package com.cosmoport.cosmocore.controller.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API error object.
 *
 * @since 0.0.1
 */
public record ApiErrorDto(String code, String message) {
    @JsonCreator
    public ApiErrorDto(@JsonProperty("code") String code, @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }
}
