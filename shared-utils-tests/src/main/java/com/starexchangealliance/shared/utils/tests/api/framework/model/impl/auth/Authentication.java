package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.auth;

import com.google.common.base.Optional;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

import java.util.ArrayList;
import java.util.List;

public class Authentication implements Block {

    private Optional<String> credentials;
    private List<Block> blocks;
    private String comment;

    public Authentication() {
        this(Optional.<String>absent(), new ArrayList<Block>());
    }

    public Authentication(Optional<String> credentials, List<Block> blocks) {
        this.credentials = credentials;
        this.blocks = blocks;
    }

    public Optional<String> getCredentials() {
        return credentials;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setCredentials(Optional<String> credentials) {
        this.credentials = credentials;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
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
