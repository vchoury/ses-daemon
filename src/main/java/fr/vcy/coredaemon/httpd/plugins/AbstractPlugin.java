package fr.vcy.coredaemon.httpd.plugins;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpRequest.RequestVisitor;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.services.ProcessMonitor;

/**
 *
 * @author VCHOURY
 */
public abstract class AbstractPlugin implements RequestVisitor {

    public final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    private ProcessMonitor pm;
    private String context;
    private String description;
    
    public AbstractPlugin(String ctx, String desc) {
        this.context = ctx.toLowerCase();
        if (!context.startsWith("/")) {
            context = "/" + context;
        }
        while (context.endsWith("/")) {
            context = context.substring(0, context.length() - 1);
        }
        this.description = desc;
    }
    
    public ProcessMonitor getProcessMonitor() {
		return pm;
	}

	public void setProcessMonitor(ProcessMonitor pm) {
		this.pm = pm;
	}

	public String getContext() {
        return this.context;
    }
    
    public void prepareShutdown() {
        
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void visit(HttpRequest request) {
    	if (StringUtils.startsWithIgnoreCase(request.getUri(), getContext() + "/")
    			|| StringUtils.equalsIgnoreCase(request.getUri(), getContext())) {
			try {
				if (pm != null) {
					pm.startMonitoring(this, request.getTimeout());
				}
				HttpResponse res = doServe(request);
		    	request.setResponse(res);
			} catch (Exception ex) {
	            LOGGER.error(request.toString(), ex);
			} finally {
				if (pm != null) {
					pm.stopMonitoring(this);
				}
			}
    	}
    }
    
    public abstract String usage();
    
    public abstract HttpResponse doServe(HttpRequest request) throws Exception;
    
    /**
     * Le path ne commence ni ne fini par un /
     */
    protected String getLocalPathFromUri(String uri) {
        String path = uri.trim().replace(File.separatorChar, '/');
        // Remove context
        if (StringUtils.startsWithIgnoreCase(uri, getContext())) {
            path = uri.substring(getContext().length());
        }
        // Remove URL arguments
        if (path.indexOf('?') >= 0) {
            path = path.substring(0, path.indexOf('?'));
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
}
