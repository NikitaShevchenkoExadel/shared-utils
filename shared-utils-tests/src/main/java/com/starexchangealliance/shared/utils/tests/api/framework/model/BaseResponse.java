package com.starexchangealliance.shared.utils.tests.api.framework.model;

import com.google.common.base.Optional;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseResponse implements Response {

    private int code;
    private Optional<File> responseFile;
    private Map<String, String> responseHeaders;

    public BaseResponse(int code, Optional<File> responseFile, Map<String, String> responseHeaders) {
        this.code = code;
        this.responseFile = responseFile;
        this.responseHeaders = responseHeaders;
    }

    @Override
    public final int getCode() {
        return code;
    }

    @Override
    public final Optional<File> getResponseFile() {
        return responseFile;
    }

    @Override
    public final Map<String, String> getResponseHeaders() {
        return new LinkedHashMap<>(responseHeaders);
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public void setResponseFile(Optional<File> responseFile) {
        this.responseFile = responseFile;
    }

    public void addResponseHeader(String key, String value) {
        this.responseHeaders.put(key, value);
    }


}
