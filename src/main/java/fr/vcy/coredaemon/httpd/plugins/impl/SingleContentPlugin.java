package fr.vcy.coredaemon.httpd.plugins.impl;

import java.io.ByteArrayInputStream;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;

/**
 *
 * @author vchoury
 */
public class SingleContentPlugin extends AbstractPlugin {

    private final byte[] content;
    private String mime;
    
    public SingleContentPlugin(String ctx, String desc, byte[] c, String mime) {
        super(ctx, desc);
        this.content = new byte[c.length];
        System.arraycopy(c, 0, this.content, 0, c.length);
        this.mime = mime;
    }
    
    @Override
    public String usage() {
        return "GET " + getContext();
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
         if (request.getMethod() == Method.GET) {
            return new HttpResponse(Status.OK, mime, new ByteArrayInputStream(content));
        } else {
            return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
        }
    }
    
}
