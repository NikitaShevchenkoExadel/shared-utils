package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.execute;

import com.google.common.base.Optional;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

import java.io.File;

public class ExecuteOperation implements Block {

    protected String className;
    protected Optional<File> file = Optional.absent();
    protected String comment = "";

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setClassName(String name) {
        this.className = name;
    }

    public String getClassName() {
        return className;
    }

    public Optional<File> getFile() {
        return file;
    }

    public void setFile(Optional<File> file) {
        this.file = file;
    }
}
