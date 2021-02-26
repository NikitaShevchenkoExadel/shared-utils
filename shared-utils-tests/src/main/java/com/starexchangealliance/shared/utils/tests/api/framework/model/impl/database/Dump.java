package com.starexchangealliance.shared.utils.tests.api.framework.model.impl.database;

import com.google.common.base.Optional;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dump implements Block {

    private Optional<File> file;
    private List<String> listSQL;
    private String comment;

    public Dump() {
        this(Optional.<File>absent(), new ArrayList<String>());
    }

    public Dump(Optional<File> file, List<String> listSQL) {
        this.file = file;
        this.listSQL = listSQL;
    }

    public Optional<File> getFile() {
        return file;
    }

    public void setFile(Optional<File> file) {
        this.file = file;
    }

    public List<String> getSql() {
        return new ArrayList<>(listSQL);
    }

    public void addSQL(String sql) {
        this.listSQL.add(sql);
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
