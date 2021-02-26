package com.starexchangealliance.shared.utils.tests.api.framework.model;

import com.google.common.base.Optional;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BasePresentBodyRequest extends BaseResponse implements HttpMethodBlock, Body {

    private String url;
    private Map<String, String> requestHeaders;
    private boolean multipart;
    private Map<String, String> params;
    private Map<String, File> files;
    private String comment;

    public BasePresentBodyRequest() {
        this("", new LinkedHashMap<>(), false,
                new LinkedHashMap<>(), new LinkedHashMap<>(), 0,
                Optional.absent(), new LinkedHashMap<>());
    }


    public BasePresentBodyRequest(String url,
                                  Map<String, String> requestHeaders,
                                  boolean multipart,
                                  Map<String, String> params,
                                  Map<String, File> files,
                                  int code,
                                  Optional<File> responseFile,
                                  Map<String, String> responseHeaders) {
        super(code, responseFile, responseHeaders);
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.multipart = multipart;
        this.params = params;
        this.files = files;
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
    public final boolean getMultipart() {
        return multipart;
    }

    @Override
    public final Map<String, String> getParams() {
        return new LinkedHashMap<>(params);
    }

    @Override
    public final Map<String, File> getFiles() {
        return new LinkedHashMap<>(files);
    }

    @Override
    public void setMultipart(boolean flag) {
        this.multipart = flag;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void addParam(String key, String value) {
        this.params.put(key, value);
    }

    @Override
    public void addFile(String key, File value, Optional<String> fileName) {
        this.files.put(key, value);
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
