package com.starexchangealliance.shared.utils.tests;

import org.imagination.comparator.Comparator;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class TreeComparator {

    private TreeComparator() {
        // hidden
    }

    public static void compare(String expected, String actual) throws ComparisonException {
        try {
            strict(Collections.emptyMap()).compare(expected, actual);
        } catch (Throwable t) {
            throw new ComparisonException(t.getMessage());
        }
    }

    public static Comparator strict(Map<String, Pattern> aliases) {
        return Comparator.java().strict(aliases);
    }

    public static class ComparisonException extends RuntimeException {
        public ComparisonException(String message) {
            super(message);
        }
    }

}
