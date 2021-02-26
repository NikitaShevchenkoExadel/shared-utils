package com.starexchangealliance.shared.utils.tests.api.framework.parser;

import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigCollector {

    private final File root;
    private final Pattern pattern;

    public ConfigCollector(File root, Pattern pattern) {
        this.root = root;
        this.pattern = pattern;
    }

    public Result collect() {
        List<File> configs = new ArrayList<>();
        walk(root, configs);

        Result result = new Result();
        for (File each : configs) {
            apply(each, result);
        }
        return result;
    }

    private void apply(File each, Result result) {
        try {
            Config config = new ConfigParser(root).parse(each);
            result.add(each, new MappingResult(config, null));
        } catch (Exception e) {
            result.add(each, new MappingResult(null, e));
        }
    }

    private void walk(File root, List<File> configs) {
        File[] list = root.listFiles();
        if (list == null) return;

        // sort the tests into alphabetical order (listFiles will be random)
        Arrays.sort(list);

        for (File file : list) {
            if (file.isDirectory()) {
                walk(file, configs);
            } else {
                if (file.getName().equals("config.xml") && matchesPathPattern(file)) {
                    configs.add(file);
                }
            }
        }
    }

    private boolean matchesPathPattern(File each) {
        String pathFromRoot = each.getPath().replace(root.getPath(), "");
        return pattern.matcher(pathFromRoot).matches();
    }

    public class Result {
        private final Map<File, MappingResult> map = new LinkedHashMap<>();

        public void add(File file, MappingResult mappingResult) {
            map.put(file, mappingResult);
        }

        public Map<File, MappingResult> get() {
            return map;
        }
    }

    public class MappingResult {

        public final Config config;
        public final Exception exception;

        MappingResult(Config config, Exception exception) {
            this.config = config;
            this.exception = exception;
        }
    }
}
