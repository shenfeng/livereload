package me.shenfeng.livereload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import clojure.lang.IFn;

public class PollingWatcher implements Runnable {

    private Map<File, Long> files = new HashMap<File, Long>(1024);
    private final File root;
    private final IFn cb;

    private void addRecusive(Map<File, Long> map, File folder) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                addRecusive(map, f);
            } else {
                map.put(f, f.lastModified());
            }
        }
    }

    public PollingWatcher(String root, IFn fn) {
        this.root = new File(root);
        this.cb = fn;
        if (!this.root.isDirectory()) {
            System.err.println(root + " is not a folder");
            System.exit(1);
        }
        addRecusive(files, this.root);
    }

    public void run() {
        for (;;) {
            Map<File, Long> tmp = new HashMap<File, Long>(files.size() * 2);
            addRecusive(tmp, root);
            List<FileEvent> events = new ArrayList<FileEvent>();
            Iterator<Entry<File, Long>> ite = tmp.entrySet().iterator();
            while (ite.hasNext()) {
                Entry<File, Long> e = ite.next();
                Long t = files.get(e.getKey());
                if (t == null) {
                    events.add(new FileEvent(e.getKey(), FileEvent.CREATE));
                } else if (t < e.getValue()) {
                    events.add(new FileEvent(e.getKey(), FileEvent.MODIFY));
                }
                files.remove(e.getKey());
            }
            for (File f : files.keySet()) {
                events.add(new FileEvent(f, FileEvent.DELETE));
            }
            files = tmp;
            FileWatcher.call(cb, events);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
