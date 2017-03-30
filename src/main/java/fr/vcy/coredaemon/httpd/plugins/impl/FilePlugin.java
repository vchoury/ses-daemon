package fr.vcy.coredaemon.httpd.plugins.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;

/**
 *
 * @author VCHOURY
 */
public class FilePlugin extends AbstractPlugin {

    private File rootDir;
    private boolean readonly;

    public FilePlugin(String ctx, File wwwroot) {
        this(ctx, "Serveur de fichiers sur " + wwwroot.getPath(), wwwroot, false);
    }

    public FilePlugin(String ctx, String desc, File wwwroot) {
        this(ctx, desc, wwwroot, false);
    }

    public FilePlugin(String ctx, String desc, File wwwroot, boolean readonly) {
        super(ctx, desc);
        this.readonly = readonly;
        this.rootDir = wwwroot;
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("GET /files/<rep> : ").append("Affiche la liste des fichiers dans le répertoire <rep>").append("\n");
        sb.append("GET /files/<rep>/<f> : ").append("Retourne le contenu du fichier <f> du répertoire <rep>").append("\n");
        if (!readonly) {
            sb.append("PUT /files/<rep>/<f> : ").append("Créé ou modifie le fichier <f> du répertoire <rep>").append("\n");
            sb.append("DELETE /files/<rep>/<f> : ").append("Supprime le fichier <f> du répertoire <rep>");
        }
        return sb.toString();
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all request.getHeader()s and HTTP parameters.
     */
    @Override
    public HttpResponse doServe(HttpRequest request) throws Exception {
        // Make sure we won't die of an exception later
        if (!getRootDir().isDirectory()) {
            return new HttpResponse(HttpHelper.Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, getRootDir().getPath() + " is not a directory!");
        }
        String path = getLocalPathFromUri(request.getUri());
        // Prohibit getting out of current directory
        if (path.startsWith("src/main") || path.endsWith("src/main") || path.contains("../")) {
            return new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, path + " is an incorrect path.");
        }
        switch (request.getMethod()) {
            case GET: 
                return getFile(request.getHeader(), path);
            case POST:
                if (!readonly && request.getParms().containsKey("request.getMethod()") && !request.getMethod().equals(Method.valueOf(request.getParms().get("request.getMethod()")))) { 
                    return doServe(request);
                }
                if (!readonly && request.getParms().containsKey("do") && ("X".equalsIgnoreCase(request.getParms().get("do")) || "Supprimer".equalsIgnoreCase(request.getParms().get("do")))) { 
                    return doServe(request);
                }
                if (!readonly && !path.isEmpty()) {
                    return editFile(request.getHeader(), path);
                }
                break;
            case PUT:
                if (!readonly && request.getParms().containsKey("pj")) {
                    return putFile(request.getHeader(), path, request.getParms().get("pj"), request.getFiles().get("pj"));
                }
                if (!readonly && !path.isEmpty() && request.getParms().containsKey("content")) {
                    String content = request.getParms().get("content");
                    return putFile(request.getHeader(), path, content);
                }
                if (!readonly && !path.isEmpty() && !request.getFiles().isEmpty()) {
                    return putFile(request.getHeader(), path, request.getFiles());
                }
                break;
            case DELETE:
                if (!readonly && !path.isEmpty()) {
                    return deleteFile(request.getHeader(), path);
                }
                break;
            default:
                break;
        }
        return new HttpResponse(HttpHelper.Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
    }

    protected HttpResponse getFile(Map<String, String> header, String path) {
        HttpResponse res = null;
        File f = new File(getRootDir(), path);
        if (res == null && !f.exists()) {
            res = new HttpResponse(HttpHelper.Status.NOT_FOUND, HttpHelper.MIME_PLAINTEXT, "Error 404, file not found.");
        }

        // List the directory, if necessary
        if (res == null && f.isDirectory()) {
            // First try index.html and index.htm
            if (new File(f, "index.html").exists()) {
                f = new File(f, "index.html");
            } else if (new File(f, "index.htm").exists()) {
                f = new File(f, "index.htm");
            } else if (f.canRead()) {
                // No index file, list the directory if it is readable
                List<String> files = Arrays.asList(f.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isFile();
                    }
                }));
                Collections.sort(files);
                List<String> directories = Arrays.asList(f.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isDirectory();
                    }
                }));
                Collections.sort(directories);

                String title = "Root Directory";
                String up = null;
                if (!path.isEmpty()) {
                    title = path;
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    up = "<li><a rel='directory' href='" + getContext() + path.substring(0, path.lastIndexOf('/') + 1) + "'><span class='dirname'>..</span></a></b></li>";
                }
                StringBuilder msg = new StringBuilder();
                if (up != null || directories.size() + files.size() > 0) {
                    msg.append("<ul>");
                    if (up != null || directories.size() > 0) {
                        msg.append("<section class='directories'>");
                        if (up != null) {
                            msg.append(up);
                        }
                        for (String directory : directories) {
                            msg.append("<li><a rel='directory' href='").append(HtmlUtils.encodeUri(getContext() + path + "/" + directory)).append("'><span class='dirname'>").append(directory).append("</span></a></b></li>");
                        }
                        msg.append("</section>");
                    }
                    if (files.size() > 0) {
                        msg.append("<section class='files'>");
                        for (String file : files) {
                            String target = HtmlUtils.encodeUri(getContext() + path + "/" + file);
                            msg.append("<form class='form' HttpMethod='post' action='").append(target).append("'>");
                            msg.append("<li><a href='").append(target).append("'><span class='filename'>").append(file).append("</span></a>");
                            File curFile = new File(f, file);
                            msg.append("<span class='filesize'>&nbsp;[").append(FileUtils.byteCountToDisplaySize(curFile.length())).append("]</span>");
                            if (!readonly) {
                                msg.append("  <input class='icon edit' type='submit' name='do' title='Editer' value='E' />");
                                msg.append("  <input class='icon delete' type='submit' name='do' title='Supprimer' value='X' />");
                            }
                            msg.append("</li>");
                            msg.append("</form>");
                        }
                        msg.append("</section>");
                    }
                    msg.append("</ul>");
                }
                HtmlPage page = new HtmlPage();
                page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
                page.setBodyHeaderH3(getDescription());
                page.addHtmlContent(title, msg.toString());
                if (!readonly) {
                    page.addRequestForm("Ajouter un fichier", HtmlUtils.encodeUri(getContext() + path), "PUT", true, null, null);
                }
                return new HttpResponse(page.toString());
            } else {
                res = new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0) {
                    mime = HttpHelper.MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                }
                if (mime == null) {
                    mime = HttpHelper.MIME_DEFAULT_BINARY;
                }

                // Calculate etag
                String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.get("range");
                if (range != null && range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                // Change return code and add Content-Range request.getHeader() when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new HttpResponse(HttpHelper.Status.RANGE_NOT_SATISFIABLE, HttpHelper.MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                        res.addHeader("ETag", etag);
                    } else {
                        if (endAt < 0) {
                            endAt = fileLen - 1;
                        }
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) {
                            newLen = 0;
                        }

                        final long dataLen = newLen;
                        FileInputStream fis = new FileInputStream(f) {

                            @Override
                            public int available() throws IOException {
                                return (int) dataLen;
                            }
                        };
                        fis.skip(startFrom);

                        res = new HttpResponse(HttpHelper.Status.PARTIAL_CONTENT, mime, new BufferedInputStream(fis));
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                } else {
                    if (etag.equals(header.get("if-none-match"))) {
                        res = new HttpResponse(HttpHelper.Status.NOT_MODIFIED, mime, "");
                    } else {
                        res = new HttpResponse(HttpHelper.Status.OK, mime, new FileInputStream(f));
                        res.addHeader("Content-Length", "" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                }
            }
        } catch (IOException ioe) {
            res = new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed : " + ioe.toString());
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }

    protected HttpResponse editFile(Map<String, String> header, String path) {
        File destFile = new File(getRootDir(), path);
        if (destFile.isDirectory()) {
            return new HttpResponse(HttpHelper.Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Can't edit directory : " + path);
        }
        String html = "<b><a href='" + getContext() + "/" + path.substring(0, path.lastIndexOf('/') + 1) + "'>..</a></b><br/><br/>";
        html += "<form HttpMethod='POST'>";
        html += "<input type='hidden' name='request.getMethod()' id='request.getMethod()' readonly='readonly' value='PUT' />";
        html += "<textarea name='content' cols='100' rows='20'>";
        if (destFile.isFile()) {
            try {
                html += StringEscapeUtils.escapeHtml4(FileUtils.readFileToString(destFile, "UTF-8"));
            } catch (IOException ex) {
                return new HttpResponse(HttpHelper.Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, "Can't read file : " + path + " (" + ex.toString() + ")");
            }
        }
        html += "</textarea><br/>";
        html += "<input type='submit' name='do' value='Sauver' />";
        HtmlPage page = new HtmlPage();
        page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
        page.setBodyHeaderH3(getDescription());
        page.addHtmlContent("Edition : " + path, html);
        return new HttpResponse(page.toString());
    }
    
    protected HttpResponse putFile(Map<String, String> header, String path, Map<String, String> files) {
        File destFile = new File(getRootDir(), path);
        for (String srcPath : files.values()) {
            if (srcPath.isEmpty()) {
                destFile.mkdirs();
            } else {
                try {
                    destFile.getParentFile().mkdirs();
                    File srcFile = new File(srcPath);
                    if (!srcFile.exists()) {
                        destFile.createNewFile();
                    } else {
                        FileUtils.copyFile(srcFile, destFile);
                        FileUtils.deleteQuietly(srcFile);
                    }
                } catch (IOException ex) {
                    return new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "Create file failed : " + ex.toString());
                }
            }
        }
        return new HttpResponse("<html><body><a href='" + getContext() + "/" + path + "'>" + path + "</a></body></html>");
    }

    protected HttpResponse putFile(Map<String, String> header, String path, String filename, String tmpFilePath) {
        File destFile = new File(getRootDir() + "/" + path, filename);
        File srcFile = new File(StringUtils.defaultString(tmpFilePath, ""));
        try {
            destFile.getParentFile().mkdirs();
            if (!srcFile.exists()) {
                destFile.mkdir();
            } else {
                FileUtils.copyFile(srcFile, destFile);
                FileUtils.deleteQuietly(srcFile);
            }
        } catch (IOException ex) {
            return new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "Create file failed : " + ex.toString());
        }
        return new HttpResponse("<html><body><a href='" + getContext() + "/" + path + "/" + filename + "'>" + path + "/" + filename + "</a></body></html>");
    }

    protected HttpResponse putFile(Map<String, String> header, String path, String content) {
        File destFile = new File(getRootDir(), path);
        if (content != null && !content.isEmpty()) {
            try {
                FileUtils.writeStringToFile(destFile, content, "UTF-8");
                return new HttpResponse("<html><body><a href='" + getContext() + "/" + path + "'>" + path + "</a></body></html>");
            } catch (IOException ex) {
                return new HttpResponse(HttpHelper.Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, "Can't write in file : " + path + " (" + ex.toString() + ")");
            }
        } else {
            return new HttpResponse(HttpHelper.Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Content empty");
        }
    }
    
    protected HttpResponse deleteFile(Map<String, String> header, String path) {
        File destFile = new File(getRootDir(), path);
        try {
            if (destFile.isFile()) {
                if (!destFile.delete()) {
                    throw new IOException("Delete failed on " + destFile.getPath());
                }
            } else if (destFile.isDirectory()) {
                FileUtils.deleteDirectory(destFile);
            } else {
                return new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "File doesn't exist.");
            }
        } catch (IOException ex) {
            return new HttpResponse(HttpHelper.Status.FORBIDDEN, HttpHelper.MIME_PLAINTEXT, "Delete file failed : " + ex.toString());
        }
        String parent = path.substring(0, path.lastIndexOf('/') + 1);
        return new HttpResponse("<html><body><a href='" + getContext() + "/" + parent + "'>" + parent + "</a></body></html>");
    }

}
