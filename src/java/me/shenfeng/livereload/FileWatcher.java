package me.shenfeng.livereload;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import clojure.lang.IFn;

public class FileWatcher {

    public static void main(String[] args) throws IOException, InterruptedException {

        // ProcessBuilder b = new ProcessBuilder("ls", "-l");
        // b.start()
        // new FileWatcher(".", new ArrayList<Pattern>()).start();
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private final WatchService watcher;
    private final List<Pattern> ignores;
    private final IFn cb;
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();

    public FileWatcher(String root, List<Pattern> ignores, IFn cb) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.cb = cb;
        this.ignores = ignores;
        Path r = Paths.get(root);
        System.out.println(r.toAbsolutePath());
        registerAll(r);
    }

    public void registerAll(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void reportEvent(Kind<?> kind, Path path) {
        boolean report = true;
        for (Pattern p : ignores) {
            if (p.matcher(path.toString()).find()) {
                report = false;
                break;
            }
        }
        if (report) {
            cb.invoke(kind.name(), path.toString());
        }
    }

    public void start() throws InterruptedException {
        for (;;) {

            WatchKey key = watcher.take();

            Path dir = keys.get(key);

            for (WatchEvent<?> event : key.pollEvents()) {
                Kind<?> kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                reportEvent(kind, child);
                // print out event
                System.out.format("%s: %s\n", kind.name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
