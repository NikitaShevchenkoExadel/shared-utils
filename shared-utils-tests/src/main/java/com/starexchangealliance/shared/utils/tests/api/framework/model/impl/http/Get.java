package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http;

import com.starexchangealliance.shared.utils.tests.api.framework.model.BaseAbsentBodyRequest;

public class Get extends BaseAbsentBodyRequest {

    public Get() {
        super();
    }

    @Override
    public String getName() {
        return "GET";
    }


}
