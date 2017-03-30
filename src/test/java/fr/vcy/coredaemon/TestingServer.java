package fr.vcy.coredaemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.httpd.HttpServer;

public class TestingServer {

    public static Logger LOGGER = LoggerFactory.getLogger(TestingServer.class.getName());
    
    protected static CfgBase cfg;
    protected static HttpServer server;
    
	public static void setUp(int port) throws Exception {
        cfg = new CfgBase();
        cfg.loadTestProperties();
        System.setProperty("vcy.base.server.port", "" + port);
    	server = new HttpServer();
        server.init(cfg);
        server.start(cfg);
	}

	public static void tearDown() throws Exception {
		server.stop(cfg);
	}
    
	public static String getAdress() {
		return "http://" + cfg.getHost() + ":" + cfg.getPort();
	}

	public static CfgBase getCfg() {
		return cfg;
	}

	public static HttpServer getServer() {
		return server;
	}
    
}
