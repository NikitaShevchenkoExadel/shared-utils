package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http;

import com.starexchangealliance.shared.utils.tests.api.framework.model.BasePresentBodyRequest;
import com.starexchangealliance.shared.utils.tests.api.framework.model.HttpMethodBlock;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Response;

public class Patch extends BasePresentBodyRequest implements HttpMethodBlock, Response {

    public Patch() {
        super();
    }

    @Override
    public String getName() {
        return "PATCH";
    }
}
