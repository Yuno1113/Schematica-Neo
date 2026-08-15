package com.yuno.schematicaneo;

import java.io.File;
import java.io.FileFilter;

import com.yuno.schematicaneo.handler.ConfigurationHandler;

public class FileFilterSchematic implements FileFilter {

    private final boolean directory;

    public FileFilterSchematic(boolean dir) {
        this.directory = dir;
    }

    @Override
    public boolean accept(File file) {
        if (this.directory) {
            return file.isDirectory();
        }
        if (ConfigurationHandler.useSchematicplusFormat) {
            return file.getName()
                .toLowerCase()
                .endsWith(".schemplus");
        } else {
            return file.getName()
                .toLowerCase()
                .endsWith(".schematic");
        }
    }
}
