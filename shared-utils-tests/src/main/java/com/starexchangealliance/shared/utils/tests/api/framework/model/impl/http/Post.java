package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http;

import com.starexchangealliance.shared.utils.tests.api.framework.model.BasePresentBodyRequest;

public class Post extends BasePresentBodyRequest {

    public Post() {
        super();
    }

    @Override
    public String getName() {
        return "POST";
    }

}
