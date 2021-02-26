package com.starexchangealliance.shared.utils.tests.api.framework.model;

import com.google.common.base.Optional;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseAbsentBodyRequest extends BaseResponse implements HttpMethodBlock {

    private String url;
    private Map<String, String> requestHeaders;
    private String comment;

    public BaseAbsentBodyRequest() {
        this("", new LinkedHashMap<String, String>(), 0,
                Optional.<File>absent(),
                new LinkedHashMap<String, String>());
    }

    public BaseAbsentBodyRequest(String url,
                                 Map<String, String> requestHeaders,
                                 int code,
                                 Optional<File> responseFile,
                                 Map<String, String> responseHeaders) {
        super(code, responseFile, responseHeaders);
        this.url = url;
        this.requestHeaders = requestHeaders;
    }

    @Override
    public final String getUrl() {
        return url;
    }

    @Override
    public final Map<String, String> getRequestHeaders() {
        return new LinkedHashMap<>(requestHeaders);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void addRequestHeader(String key, String value) {
        this.requestHeaders.put(key, value);
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
