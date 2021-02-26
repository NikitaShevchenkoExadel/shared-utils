package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.instructruction;

import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

public class Break implements Block {

    private String comment;

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return null;
    }
}
