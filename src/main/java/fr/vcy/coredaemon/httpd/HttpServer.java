package fr.vcy.coredaemon.httpd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;
import fr.vcy.coredaemon.httpd.HttpRequest.RequestVisitor;
import fr.vcy.coredaemon.httpd.nano.ServerProxy;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.plugins.PluginManager;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.services.ProcessMonitor;

/** 
 * 
 * Lightweight server
 * 
 * usage : 
 *  Server server = new Server(host, port);
 *  server.add(new MonitorPlugin());
 *  server.add(new PropertiesPlugin());
 *  server.add(new HttpFormPlugin("/requester"));
 * ...
 * server.start();
 * while (...) { } 
 * server.stop();
 * 
 * @author vchoury
 */
public class HttpServer implements Module, PluginManager, RequestVisitor {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class.getName());
    private boolean shutdownRequested = false;
    private String servername;
    private ServerProxy proxy;
    private ProcessMonitor processMonitor;
    private List<AbstractPlugin> plugins = new ArrayList<AbstractPlugin>();
    
	@Override
	public void init(CfgBase cfg) {
		this.servername = cfg.getName();
        this.shutdownRequested = false;
        proxy = new ServerProxy(cfg.getHost(), cfg.getPort(), this);
	}

	@Override
    public void start(CfgBase cfg) throws IOException {
    	proxy.start();
        LOGGER.info("Http Server started, listening on port :" + proxy.getListeningPort());
    }

	@Override
    public void stop(CfgBase cfg) throws Exception {
        shutdownRequested = true;
        for (AbstractPlugin p : plugins) {
            p.prepareShutdown();
        }
        if (proxy.isAlive()) {
        	proxy.stop();
        }
        LOGGER.info("Server stopped.");
    }

	@Override
	public void reload(CfgBase cfg) throws Exception {
		stop(cfg);
		init(cfg);
		start(cfg);
	}

	public boolean isAlive() {
		return proxy.isAlive();
	}
    
    public String getServername() {
        return servername;
    }
    
    public boolean isShutdownRequested() {
        return shutdownRequested;
    }
    
	@Override
	public void visit(HttpRequest request) {
        LOGGER.debug(request.toString());
        long tick = Calendar.getInstance().getTimeInMillis();
        if (isShutdownRequested()) {
        	LOGGER.debug("Server unavailable : shutdown requested");
        	request.setResponse(new HttpResponse(HttpHelper.Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, "Server unavailable : shutdown requested"));
        } else if (request.getUri().isEmpty() || "/".equalsIgnoreCase(request.getUri())) {
            HtmlPage page = new HtmlPage();
            request.setResponse(new HttpResponse(page.toString()));
        } else {
            for (AbstractPlugin p : plugins) {
				p.visit(request);
            }
            if (request.getResponse() == null) {
            	request.setResponse(new HttpResponse(HttpHelper.Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, "Unable to resolve request : " + request.toString()));
            }
        }
        LOGGER.debug(" -> " + request.getResponse().getStatus().getDescription() 
                + " [" + (Calendar.getInstance().getTimeInMillis() - tick) + "ms]");
    }

    public List<AbstractPlugin> getPlugins() {
    	return plugins;
    }

    public boolean add(AbstractPlugin p) {
        return add(p, true);
    }

    public boolean add(AbstractPlugin p, boolean browsable) {
        for (AbstractPlugin existing : plugins) {
            if (StringUtils.startsWithIgnoreCase(p.getContext(), existing.getContext())
                    || StringUtils.startsWithIgnoreCase(existing.getContext(), p.getContext())) {
                return false;
            }
        }
        boolean added = plugins.add(p);
        if (added) {
        	p.setProcessMonitor(processMonitor);
            LOGGER.info("Added : " + p.getClass().getSimpleName() + " " + p.getContext() + " (" + p.getDescription() + ")");
            if (browsable) {
                HtmlPage.addNav(p.getContext(), p.getContext());
            }
        }
        return added;
    }

    public boolean remove(AbstractPlugin p) {
        boolean removed = plugins.remove(p);
        if (removed) {
            LOGGER.info("Removed " + p.getClass().getSimpleName() + " " + p.getContext());
            HtmlPage.removeNav(p.getContext());
        }
        return removed;
    }

	@Override
	public boolean remove(String ctx) {
        for (AbstractPlugin pex : plugins) {
            if (StringUtils.equalsIgnoreCase(ctx, pex.getContext())) {
            	return remove(pex);
            }
        }
        return false;
	}

	public void setProcessMonitor(ProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
	}

}
