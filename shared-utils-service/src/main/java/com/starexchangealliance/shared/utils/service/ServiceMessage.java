package com.starexchangealliance.shared.utils.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceMessage {
    private final String message;

    public static ServiceMessage of(String message) {
        return new ServiceMessage(message);
    }
}
