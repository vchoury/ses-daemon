package fr.vcy.coredaemon;


public interface Module {

	public void init(CfgBase cfg) throws Exception;
	
	public void start(CfgBase cfg) throws Exception;
	
	public void stop(CfgBase cfg) throws Exception;

	public void reload(CfgBase cfg) throws Exception;
	
}
