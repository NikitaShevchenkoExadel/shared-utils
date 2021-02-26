package com.starexchangealliance.shared.utils.service;

import lombok.Getter;

@Getter
public class ServiceOutput extends RuntimeException {

    private final ServiceStatus apiCode;
    private final Object object;

    ServiceOutput(ServiceStatus apiCode, Object object) {
        this.apiCode = apiCode;
        this.object = object;
    }

}
