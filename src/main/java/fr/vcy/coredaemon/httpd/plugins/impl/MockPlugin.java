package fr.vcy.coredaemon.httpd.plugins.impl;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;

/**
 *
 * @author vchoury
 */
public class MockPlugin extends AbstractPlugin {

    private HttpResponse response;
    private long delay;
    
    public MockPlugin(String ctx, HttpResponse resp, long millis) {
        super(ctx, "Plugin de test");
        this.response = resp;
        this.delay = millis;
    }
    
    @Override
    public String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getContext()).append(" : Attends ").append(delay).append("ms et retourne ").append(response.getStatus().toString()).append("\n");
        return sb.toString();
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            return new HttpResponse("Interrupted");
        }
        return response;
    }
    
}
