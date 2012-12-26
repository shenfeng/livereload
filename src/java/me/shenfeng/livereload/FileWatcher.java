package me.shenfeng.livereload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;

public class FileWatcher {
    public static Thread start(String root, IFn cb) throws IOException {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");
        boolean java7 = System.getProperty("java.version").toLowerCase().startsWith("1.7");
        Thread t;
        if (java7 && !mac) { // mac has latency
            t = new Thread(new WatchServiceWatcher(root, cb), "file-watcher");
        } else {
            t = new Thread(new PollingWatcher(root, cb), "file-watcher");
        }
        t.start();
        return t;
    }

    public static final Keyword FILE = Keyword.intern("file");
    public static final Keyword EVENT = Keyword.intern("event");

    public static void call(IFn cb, List<FileEvent> events) {
        if (events.size() > 0) {
            List<IPersistentMap> es = new ArrayList<IPersistentMap>(events.size());
            for (FileEvent e : events) {
                es.add(PersistentArrayMap.createWithCheck(new Object[] { FILE, e.file, EVENT,
                        e.event }));
            }
            cb.invoke(es);
        }
    }
}
