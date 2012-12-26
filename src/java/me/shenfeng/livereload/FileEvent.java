package me.shenfeng.livereload;

import java.io.File;

public class FileEvent {

    public static final String CREATE = "CREATE";
    public static final String MODIFY = "MODIFY";
    public static final String DELETE = "DELETE";

    public File file;

    public FileEvent(File file, String event) {
        this.file = file;
        this.event = event;
    }

    public final String event;
}
