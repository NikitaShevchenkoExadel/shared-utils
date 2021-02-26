package com.starexchangealliance.shared.utils.service;


import lombok.Getter;

import java.util.List;

@Getter
public enum ServiceStatus {
    INTERNAL_ERROR(500, "Internal Server Error"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(403, "Not Found"),
    FORBIDDEN(404, "Forbidden"),
    ;

    private final int httpCode;
    private final String defaultMessage;

    ServiceStatus(int httpCode, String defaultMessage) {
        this.httpCode = httpCode;
        this.defaultMessage = defaultMessage;
    }

    public static ServiceStatus fromCode(Integer statusCode) {
        if (statusCode != null) {
            for (ServiceStatus each : values()) {
                if (each.httpCode == statusCode) {
                    return each;
                }
            }
        }
        return null;
    }

    public ServiceOutput with(List<PropertyServiceMessage> propertyIssues) {
        return new ServiceOutput(this, propertyIssues);
    }

    public ServiceOutput with(PropertyServiceMessage propertyIssue) {
        return new ServiceOutput(this, propertyIssue);
    }

    public ServiceOutput with(String message) {
        return new ServiceOutput(this, ServiceMessage.of(message));
    }

    public ServiceOutput withDefaultMessage() {
        return new ServiceOutput(this, ServiceMessage.of(defaultMessage));
    }

}
