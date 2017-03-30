package fr.vcy.coredaemon.httpd.plugins.impl;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdmk.comm.HtmlAdaptorServer;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;

/**
 *
 * @author vchoury
 */
public class JmxPlugin extends AbstractPlugin {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(JmxPlugin.class);
    private String host;
    private int port;
    private HtmlAdaptorServer adapter = new HtmlAdaptorServer();

    public JmxPlugin(String host, int port) {
        this("/jmx", host, port);
    }
    
    public JmxPlugin(String ctx, String host, int port) {
        super(ctx, "Démarre un adapter JMX");
        this.host = host;
        this.port = port;
        try {
            adapter.setPort(port);
            ManagementFactory.getPlatformMBeanServer().registerMBean(adapter, new ObjectName("Adaptor:name=html,port=" + port));
        } catch (Exception ex) {
            LOGGER.error("MBean Register fail", ex);
        }
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                adapter.start();
            }
        });
        serverThread.start();
    }
    
    @Override
    public String usage() {
        return "GET " + getContext() + " : redirect to a JMX Agent view on port " + port;
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
        String redirct = "http://" + host + ":" + port;
        HttpResponse res = new HttpResponse(Status.REDIRECT, HttpHelper.MIME_HTML, 
                "<html><body>Redirected: <a href=\"" + redirct + "\">" + redirct + "</a></body></html>");
        res.addHeader("Location", redirct);
        return res;
    }
    
    public void prepareShutdown() {
        adapter.stop();
    }
    
}
