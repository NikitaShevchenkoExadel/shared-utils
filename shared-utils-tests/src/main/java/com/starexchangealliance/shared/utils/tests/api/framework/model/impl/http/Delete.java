package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http;

import com.starexchangealliance.shared.utils.tests.api.framework.model.BaseAbsentBodyRequest;

public class Delete extends BaseAbsentBodyRequest {

    public Delete() {
        super();
    }

    @Override
    public String getName() {
        return "DELETE";
    }

}
