package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http;

import com.starexchangealliance.shared.utils.tests.api.framework.model.BasePresentBodyRequest;

public class Put extends BasePresentBodyRequest {

    public Put() {
        super();
    }

    @Override
    public String getName() {
        return "PUT";
    }

}
