package com.starexchangealliance.shared.utils.tests.api.framework.model;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Map;

public interface Body {

    boolean getMultipart();

    Map<String, String> getParams();

    Map<String, File> getFiles();

    void addParam(String key, String value);

    void addFile(String key, File value, Optional<String> fileName);

    void setMultipart(boolean flag);
}
