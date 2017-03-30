package fr.vcy.coredaemon.httpd.plugins.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;

/**
 *
 * @author vchoury
 */
public class FileCounterPlugin extends AbstractPlugin {
    
    private File root;

    public FileCounterPlugin(String ctx, File wwwroot) {
        super(ctx, "Compteur de fichiers sur " + wwwroot.getPath());
        this.root = wwwroot;
    }
    
    @Override
    public String usage() {
        return "GET " + getContext() + " : print file count.";
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
        try {
            return countFiles(root);
        } catch (IOException ex) {
            return new HttpResponse(HttpHelper.Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, ex.toString());
        }
    }
    
    private HttpResponse countFiles(File root) throws IOException {
        if (root != null && root.exists()) {
            if (root.isDirectory()) {
                Counter counter = new Counter(null, root, 0);
                HtmlPage page = new HtmlPage();
                page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
                page.setBodyHeaderH3(getDescription());
                page.addContent(root.getCanonicalPath(), counter.toString());
                return new HttpResponse(page.toString());
            } else {
                return new HttpResponse(HttpHelper.Status.OK, HttpHelper.MIME_PLAINTEXT, root.getName() + " [" + FileUtils.byteCountToDisplaySize(root.length()) + "]");
            }
        } else {
            return new HttpResponse(HttpHelper.Status.NOT_FOUND, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
        }
    }
    
    private class Counter {
        private File file;
        private long size;
        private int count;
        private int indent;
        private Counter parent;
        private List<Counter> childs;
        
        public Counter(Counter p, File f, int i) {
            this.file = f;
            this.parent = p;
            this.childs = new ArrayList<Counter>();
            this.indent = i;
            for (File c : file.listFiles()) {
                if (c.isDirectory()) {
                    Counter cc = new Counter(this, c, i+1);
                    childs.add(cc);
                } else {
                    size += c.length();
                    count++;
                }
            }
            if (parent != null) {
                parent.update(size, count);
            }
        }

        private void update(long s, int c) {
            this.size += s;
            this.count += c;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<indent; i++) {
                sb.append("\t");
            }
            sb.append(file.getName());
            sb.append(" [").append(count).append(" files");
            sb.append(" ; total size : ").append(FileUtils.byteCountToDisplaySize(size)).append("]");
            for (Counter c : childs) {
                sb.append("\n").append(c.toString());
            }
            return sb.toString();
        }
        
    }
    
}
