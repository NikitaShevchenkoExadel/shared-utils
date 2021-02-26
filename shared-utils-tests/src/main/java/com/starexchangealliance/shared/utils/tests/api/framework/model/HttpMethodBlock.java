package com.starexchangealliance.shared.utils.tests.api.framework.model;

import java.util.Map;

public interface HttpMethodBlock extends Block {

    String getUrl();

    Map<String, String> getRequestHeaders();

    String getName();

    void addRequestHeader(String key, String value);

    void setUrl(String url);

}
