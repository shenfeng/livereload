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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clojure.lang.IFn;

public class WatchServiceWatcher implements Runnable {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 1)
            new WatchServiceWatcher(args[0], null).run();
        else {
            new WatchServiceWatcher(".", null).run();
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private final WatchService watcher;
    private final IFn cb;
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();

    public WatchServiceWatcher(String root, IFn cb) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.cb = cb;
        Path r = Paths.get(root);
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

    public void run() {
        try {
            for (;;) {
                WatchKey key = watcher.take();
                Path dir = keys.get(key);
                List<FileEvent> events = new ArrayList<FileEvent>(2);
                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);
                    events.add(new FileEvent(child.toFile(), kind.name()));
                    if (kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                        }
                    }
                }
                FileWatcher.call(cb, events);
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Exit");
        }
    }
}
