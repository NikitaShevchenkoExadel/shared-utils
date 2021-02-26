package com.starexchangealliance.shared.utils.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {
    private final String message;

    public static Message of(String message) {
        return new Message(message);
    }
}
