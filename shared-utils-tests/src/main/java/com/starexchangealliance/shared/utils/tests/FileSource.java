package com.starexchangealliance.shared.utils.tests;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileSource extends SQLSource {

    private final StringsSource stringSource;

    public FileSource(File file) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException("File " + file + " does not exist ");
        }
        this.stringSource = new StringsSource(FileUtils.readFileToString(file, "UTF-8"));
    }

    @Override
    public List<String> getSQLs() {
        return stringSource.getSQLs();
    }
}
