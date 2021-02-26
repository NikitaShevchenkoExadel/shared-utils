package com.starexchangealliance.shared.utils.service;

import lombok.Getter;

@Getter
public final class PropertyServiceMessage extends ServiceMessage {
    private final String property;

    public PropertyServiceMessage(String property, String message) {
        super(message);
        this.property = property;
    }

    public static PropertyServiceMessage of(String property, String message) {
        return new PropertyServiceMessage(property, message);
    }
}
