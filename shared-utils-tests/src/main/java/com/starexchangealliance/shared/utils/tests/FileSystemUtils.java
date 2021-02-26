package com.starexchangealliance.shared.utils.tests;

import java.io.File;

public class FileSystemUtils {

    private FileSystemUtils() {
        // hidden
    }

    public static File locateTo(File file, String root) {
        if (file.getName().equals(root)) {
            return file;
        } else {
            return locateTo(file.getParentFile(), root);
        }
    }

    public static File locateTo(String root) {
        File userDir = new File(System.getProperty("user.dir"));
        return locateTo(userDir, root);
    }

}
