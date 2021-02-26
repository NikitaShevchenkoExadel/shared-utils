package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.mock;

import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

import java.io.File;

public class When implements Block {

    private String service;
    private File request;
    private File response;
    private String comment;

    public String getService() {
        return service;
    }

    public File getRequest() {
        return request;
    }

    public File getResponse() {
        return response;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setRequest(File request) {
        this.request = request;
    }

    public void setResponse(File response) {
        this.response = response;
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
