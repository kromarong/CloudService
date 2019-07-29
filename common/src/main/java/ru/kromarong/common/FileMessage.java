package ru.kromarong.common;

public class FileMessage {
    String filename;
    String path;

    public FileMessage(String filename, String path) {
        this.filename = filename;
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }
}
