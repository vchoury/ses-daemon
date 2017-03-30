package fr.vcy.coredaemon.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;
import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.FilePlugin;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;

/**
 *
 * @author vchoury
 */
public class CamelModule implements Module {

    public static final Logger LOGGER = LoggerFactory.getLogger(CamelModule.class.getName());
    public static final String DEFAULT_LOADBALANCER_NAME = "loadBalancer";
    public static final String DEFAULT_PLUGIN_CTX = "/camel";
    
    private String loadbalancerName;
    private int threadPoolSize;
    private ModelCamelContext camelContext;
    private Map<String, RoutesDefinition> routesMap = new HashMap<String, RoutesDefinition>();
    private FilePlugin filePlugin;

    public CamelModule(File camelDir) throws Exception {
        this(DEFAULT_PLUGIN_CTX, -1, DEFAULT_LOADBALANCER_NAME, camelDir);
    }

    public CamelModule(String ctx, int threadPoolSize, String loadBalancerName, File camelDir) throws Exception {
        this.threadPoolSize = threadPoolSize;
        this.loadbalancerName = loadBalancerName;
        this.filePlugin = new CamelFilePlugin(ctx, 
        		"Manage les routes camel dans " + camelDir.getPath(), camelDir, false);
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

	public void setLoadbalancerName(String loadbalancerName) {
		this.loadbalancerName = loadbalancerName;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@Override
	public void init(CfgBase cfg) throws Exception {
        // create camel context
        camelContext = new DefaultCamelContext(new SimpleRegistry());
        // enable verbose tracing
        if (LOGGER.isDebugEnabled()) {
            camelContext.setTracing(Boolean.TRUE);
        }
        // add system properties placeholder
        PropertiesComponent pc = new PropertiesComponent();
        camelContext.addComponent("properties", pc);
        // register load balancer
        CustomLoadBalancer.register(camelContext, loadbalancerName);
        // set threadPoolSize
        if (threadPoolSize > 0) {
            camelContext.getExecutorServiceManager().newFixedThreadPool(filePlugin.getContext(), filePlugin.getContext(), threadPoolSize);
        }
        // set thread names
        camelContext.getExecutorServiceManager().setThreadNamePattern("#camelId# (##counter#)");
        // add existing routes
        for (File xmlRoutes : filePlugin.getRootDir().listFiles()) {
            addRoutes(xmlRoutes);
        }
	}

	@Override
	public void start(CfgBase cfg) throws Exception {
        camelContext.start();
	}

	@Override
	public void stop(CfgBase cfg) throws Exception {
        camelContext.stop();
	}

	@Override
    public void reload(CfgBase cfg) throws Exception {
        for (String route : routesMap.keySet()) {
            removeRoutes(route);
        }
        for (File xmlRoutes : filePlugin.getRootDir().listFiles()) {
            addRoutes(xmlRoutes);
        }
    }

	public class CamelFilePlugin extends FilePlugin {
    
		public CamelFilePlugin(String ctx, String desc, File wwwroot, boolean readonly) {
			super(ctx, desc, wwwroot, readonly);
		}

	    @Override
	    public String usage() {
	        return "GET " + getContext() + "/ : Affiche la liste des routes"
	                + "\nGET " + getContext() + "/<route> : Visualise la route <route>"
	                + "\nPUT " + getContext() + "/<route> : Créé et démarre la route <route>"
	                + "\nDELETE " + getContext() + "/<route> : Arrete et supprime la route <route>";
	    }

		@Override
	    public HttpResponse doServe(HttpRequest request) throws Exception {
	        String path = super.getLocalPathFromUri(request.getUri());
	        HttpResponse res = super.doServe(request);
	        if (request.getMethod() == Method.PUT && res.getStatus() == Status.OK) {
	            addRoutes(new File(getRootDir(), path));
	        } else if (request.getMethod() == Method.DELETE && res.getStatus() == Status.OK) {
	            removeRoutes(path);
	        }
	        return res;
	    }
	
	    @Override
	    public void prepareShutdown() {
	    	super.prepareShutdown();
	    }

	}
	
    private void addRoutes(File xmlRoutes) throws Exception {
        InputStream is = null;
        try {
            is = new FileInputStream(xmlRoutes);
            RoutesDefinition routes = camelContext.loadRoutesDefinition(is);
            routesMap.put(xmlRoutes.getName(), routes);
            for (RouteDefinition route : routes.getRoutes()) {
                camelContext.addRouteDefinition(route);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void removeRoutes(String path) throws Exception {
        RoutesDefinition routes = routesMap.get(path);
        if (routes != null) {
            for (RouteDefinition route : routes.getRoutes()) {
                camelContext.stopRoute(route.getId(), 10, TimeUnit.SECONDS);
                camelContext.removeRoute(route.getId());
            }
            routesMap.remove(path);
        }
    }

	public AbstractPlugin getCamelPlugin() {
		return this.filePlugin;
	}

}