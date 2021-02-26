package com.starexchangealliance.shared.utils.api.dto.error;

import lombok.Getter;

@Getter
public final class PropertyMessage extends Message {
    private final String property;

    public PropertyMessage(String property, String message) {
        super(message);
        this.property = property;
    }

    public static PropertyMessage of(String property, String message) {
        return new PropertyMessage(property, message);
    }
}
