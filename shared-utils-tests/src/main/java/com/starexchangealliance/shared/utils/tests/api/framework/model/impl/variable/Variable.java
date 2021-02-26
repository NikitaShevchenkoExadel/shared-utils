package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.variable;

import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

public class Variable implements Block {

    private String name;
    private String path;
    private String comment;

    public Variable() {
        this("", "");
    }

    public Variable(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
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
