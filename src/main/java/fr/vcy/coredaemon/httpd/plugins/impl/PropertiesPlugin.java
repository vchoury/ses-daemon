package fr.vcy.coredaemon.httpd.plugins.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;
import fr.vcy.coredaemon.utils.PropertiesUtil;

/**
 *
 * @author vchoury
 */
public class PropertiesPlugin extends AbstractPlugin {

    private static final String DEFAULT_SYSTEM = "system";
    private Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

    public PropertiesPlugin() {
        this("/properties");
    }

    public PropertiesPlugin(String ctx) {
        super(ctx, "Accede et modifie les propriétés du programme");
        propertiesMap.put(DEFAULT_SYSTEM, System.getProperties());
    }

    @Override
    public String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("GET ").append(getContext()).append("<list> : ").append("Retourne la liste des clés/valeurs de la liste <list>").append("\n");
        sb.append("GET ").append(getContext()).append("<list>/<key> : ").append("Retourne la valeur de propriété <key> de la liste <list>").append("\n");
        sb.append("POST ").append(getContext()).append("<list>?<key>=<val> : ").append("Ajoute ou modifie la valeur <val> de la propriété <key> dans la liste <list>").append("\n");
        sb.append("DELETE ").append(getContext()).append("<list>/<key> : ").append("Supprime la propriété <key> de la liste <list>");
        return sb.toString();
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
        // Remove context
        String path = getLocalPathFromUri(request.getUri());
        String[] splitted = path.split("/");
        // Get property list name
        String name = splitted[0];
        if (request.getParms().containsKey("name")) {
            name = request.getParms().get("name");
        }
        // Get property key 
        String key = null;
        if (splitted.length > 1 && !splitted[1].isEmpty()) {
            key = splitted[1];
        }
        if (request.getParms().containsKey("key")) {
            key = request.getParms().get("key");
        }
        Properties props = propertiesMap.get(name);
        switch (request.getMethod()) {
            case GET:
                if (props == null || props.isEmpty()) {
                    HtmlPage page = new HtmlPage();
                    page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
                    page.setBodyHeaderH3(getDescription());
                    page.addLinkListContent("Properties", getContext(), propertiesMap.keySet().iterator());
                    page.addRequestForm("Ajouter ou modifier une propriété (penser à reload l'application)", getContext(), "POST", false, "name", DEFAULT_SYSTEM, "key", null, "value", null);
                    page.addRequestForm("Supprimer une propriété", getContext(), "DELETE", false, "name", DEFAULT_SYSTEM, "key", null);
                    return new HttpResponse(page.toString());
                } else if (key == null || key.isEmpty()) {
                    return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, PropertiesUtil.printProperties(props));
                } else {
                    return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, props.getProperty(key));
                }
            case POST:
                if (request.getParms().containsKey("method") && !request.getMethod().equals(Method.valueOf(request.getParms().get("method")))) {
                	HttpRequest newRequest = new HttpRequest(request.getUri(), request.getParms().get("method"), request.getHeader(), request.getParms(), request.getFiles());
                    return doServe(newRequest);
                }
                if (props != null && key != null && request.getParms().containsKey("value")) {
                    props.put(key, request.getParms().get("value"));
                } else if (props != null && key == null) {
                    props.putAll(request.getParms());
                }
                return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, PropertiesUtil.printProperties(props));
            case DELETE:
                if (props != null && key != null) {
                    props.remove(key);
                    return new HttpResponse(Status.OK, HttpHelper.MIME_PLAINTEXT, PropertiesUtil.printProperties(props));
                }
                break;
            default:
                break;
        }
        return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
    }
}
