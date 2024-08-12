package br.com.agdev;

import java.io.File;
import java.nio.file.Path;

public record ImageInfo(String url, Path path, boolean convert) {

    @Override
    public String toString() {
        return "path=" + path.toString().substring(path.toString().lastIndexOf(File.separator) + 1);
    }
}
