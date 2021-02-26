package com.starexchangealliance.shared.utils.tests.api.framework.model;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Map;

public interface Response {

    int getCode();

    Optional<File> getResponseFile();

    Map<String, String> getResponseHeaders();

    void addResponseHeader(String key, String value);

    void setCode(int code);

    void setResponseFile(Optional<File> file);

}
