package fr.vcy.coredaemon;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.camel.CamelModule;
import fr.vcy.coredaemon.httpd.HttpServer;
import fr.vcy.coredaemon.httpd.plugins.impl.FilePlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.JmxPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.ManagerPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.MonitorPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.ProcesserPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.PropertiesPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.RequesterPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.SingleContentPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.services.DirectoryPoller;
import fr.vcy.coredaemon.services.ProcessMonitor;

/**
 *
 * @author vchoury
 */
public class AppBase implements AppMBean, Observer {

    public static Logger LOGGER = LoggerFactory.getLogger(AppBase.class.getName());
    public static final String EXIT_FLAG_NAME = "SHUTDOWN";
    
    private CfgBase appCfg = null;
    private List<Module> modules = new ArrayList<Module>();

    public static void main(String[] args) {
        CfgBase cfg = new CfgBase();
        cfg.setMainArgs(args);
        AppBase app = new AppBase(cfg);
        app.start();
    }
    
    public AppBase(CfgBase cfg) {
        this.appCfg = cfg;
    }
    
	public CfgBase getCfg() {
	    return this.appCfg;
	}
    
    private synchronized void start() {
        LOGGER.info("******************************");
        try {
            getCfg().loadProperties();
        } catch (IOException ex) {
            LOGGER.error("Loading properties failure", ex);
            System.exit(1);
        }
        try {
			createModules();
		} catch (Exception ex) {
            LOGGER.error("Modules creation failure", ex);
            System.exit(1);
		}
        for (Module m : modules) {
            try {
            	m.init(getCfg());
            } catch (Exception ex) {
                LOGGER.error("Component initialization failure", ex);
            }
        }
        for (Module m : modules) {
            try {
            	m.start(getCfg());
            } catch (Exception ex) {
                LOGGER.error("Component start failure", ex);
            }
        }
        try {
        	this.wait();
        } catch (InterruptedException ex) {
            LOGGER.error("App interrupted", ex);
        }
        for (Module m : modules) {
            try {
            	m.stop(getCfg());
	        } catch (Exception ex) {
	            LOGGER.error("Component shutdown failure", ex);
	        }
        }
        LOGGER.info("bye bye !");
        LOGGER.info("******************************\n");
    }
    
    private synchronized void shutdown() {
    	// On reveille le thread principal pour le shutdown
        this.notify();
    }
    
    protected void createModules() throws Exception {
    	ProcessMonitor pm = new ProcessMonitor();
    	modules.add(pm);
    	DirectoryPoller poller = new DirectoryPoller(getCfg().getAdminDir());
        poller.addObserver(this);
        modules.add(poller);
    	CamelModule camel = new CamelModule(getCfg().getCamelDir());
    	modules.add(camel);
    	HttpServer server = new HttpServer();
    	server.setProcessMonitor(pm);
        createServerPlugins(server);
        server.add(camel.getCamelPlugin());
    	modules.add(server);
	}

	protected void createServerPlugins(HttpServer server) throws IOException {
        HtmlPage.setBodyHeaderH1(getCfg().getName());
        HtmlPage.addCssUrl("/style.css");
        if (getCfg().getCssFile() != null && getCfg().getCssFile().exists()) {
            byte[] b = FileUtils.readFileToByteArray(getCfg().getCssFile());
            server.add(new SingleContentPlugin("/style.css", "css stylesheet", b, "text/css"), false);
        } else {
        	server.add(new SingleContentPlugin("/style.css", "css stylesheet", HtmlUtils.getBaseCss().getBytes(), "text/css"), false);
        }
        server.add(new ManagerPlugin(server), false);
        server.add(new RequesterPlugin(), false);
        server.add(new MonitorPlugin());
        server.add(new PropertiesPlugin());
        server.add(new ProcesserPlugin() {
			@Override
			public boolean doProcess(String path) {
				return process(path);
			}
        });
        server.add(new FilePlugin("www", getCfg().getPathBase()));
        if (getCfg().getJmxPort() != null && System.getProperty("com.sun.management.jmxremote") != null) {
        	server.add(new JmxPlugin(InetAddress.getLocalHost().getHostName(), getCfg().getJmxPort()));
        }
    }

	@Override
	public void update(Observable o, Object arg) {
        if (o instanceof DirectoryPoller && arg instanceof File) {
            File flag = (File) arg;
            FileUtils.deleteQuietly(flag);
            process(flag.getName());
        }
	}
    
    public boolean process(String cmd) {
        boolean result = false;
        LOGGER.info("Processing command : " + cmd);
        if (EXIT_FLAG_NAME.equalsIgnoreCase(cmd)) {
        	shutdown();
            result = true;
        }
        LOGGER.debug("Processing command : " + cmd + " -> result = " + result);
        return result;
    }

}
