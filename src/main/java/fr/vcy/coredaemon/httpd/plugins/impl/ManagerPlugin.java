package fr.vcy.coredaemon.httpd.plugins.impl;

import org.apache.commons.lang3.StringUtils;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.plugins.PluginFactory;
import fr.vcy.coredaemon.httpd.plugins.PluginManager;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;

/**
 *
 * @author vchoury
 */
public class ManagerPlugin extends AbstractPlugin {

    private final PluginManager manager;

    public ManagerPlugin(PluginManager manager) {
        super("/manage", "Plugins manager");
        this.manager = manager;
    }

    @Override
    public String usage() {
        return "GET " + getContext() + " : manage plugin.\n"
                + "POST " + getContext() + "?type=...&context=...&arg=... : ajoute un nouveau plugin.\n"
                + "DELETE " + getContext() + "?context=... : supprime un plugin.";
    }

    @Override
    public HttpResponse doServe(HttpRequest request) throws Exception {
        switch (request.getMethod()) {
            case GET:
                HtmlPage page = new HtmlPage();
                page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
                page.setBodyHeaderH3(getDescription());
                page.addRequestForm("Create plugin", getContext(), "POST", false, "context", null, "type", null, "arg", null);
                page.addRequestForm("Delete plugin", getContext(), "DELETE", false, "context", null);
                return new HttpResponse(page.toString());
            case POST:
                if (request.getParms().containsKey("method") && !request.getMethod().equals(Method.valueOf(request.getParms().get("method")))) {
                	HttpRequest newRequest = new HttpRequest(request.getUri(), request.getParms().get("method"), request.getHeader(), request.getParms(), request.getFiles());
                    return doServe(newRequest);
                }
                String ctx = request.getParms().get("context");
                String type = request.getParms().get("type");
                String arg = request.getParms().get("arg");
                if (StringUtils.isBlank(ctx) || StringUtils.isBlank(type)) {
                    return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Please set Context and Type");
                }
                AbstractPlugin p = PluginFactory.createPlugin(type, ctx, arg);
                if (manager.add(p)) {
                    return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, "Plugin " + type + " bind to context " + ctx);
                } else {
                    return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Plugin " + type + " can't be bound to context " + ctx);
                }
            case DELETE:
                String ctx2 = request.getParms().get("context");
                if (!StringUtils.isBlank(ctx2)) {
                    if (manager.remove(ctx2)) {
                        return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, "Context " + ctx2 + " removed");
                    } else {
                        return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Context " + ctx2 + " can't be removed");
                    }
                }
                return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Unknown context " + ctx2);
            default:
                break;
        }
        return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
    }

}
