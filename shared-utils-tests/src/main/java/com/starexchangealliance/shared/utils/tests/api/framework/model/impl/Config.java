package com.starexchangealliance.shared.utils.tests.api.framework.model.impl;

import com.google.common.base.Optional;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private boolean active;
    private Optional<String> patch;
    private String description;
    private boolean onlyThis;
    private List<Block> blocks;

    public Config() {
        this(true, Optional.<String>absent(), "", false, new ArrayList<Block>());
    }

    public Config(boolean active,
                  Optional<String> patch,
                  String description,
                  boolean onlyThis,
                  List<Block> blocks) {
        this.active = active;
        this.patch = patch;
        this.description = description;
        this.onlyThis = onlyThis;
        this.blocks = blocks;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Optional<String> getPatch() {
        return patch;
    }

    public void setPatch(Optional<String> patch) {
        this.patch = patch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getOnlyThis() {
        return onlyThis;
    }

    public void setOnlyThis(boolean onlyThis) {
        this.onlyThis = onlyThis;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
    }
}
