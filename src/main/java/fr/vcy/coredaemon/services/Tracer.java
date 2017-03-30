package fr.vcy.coredaemon.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;
import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpRequest.RequestVisitor;

/**
 * En cours..
 * @author Vincent
 *
 */
public class Tracer implements Module, RequestVisitor {

    public static final Logger LOGGER = LoggerFactory.getLogger(Tracer.class.getName());

	@Override
	public void visit(HttpRequest request) {
		LOGGER.debug("je trace");
	}
	
	@Override
	public void init(CfgBase cfg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(CfgBase cfg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(CfgBase cfg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reload(CfgBase cfg) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
