package fr.vcy.coredaemon.httpd.plugins;

import java.util.List;


public interface PluginManager {

    public boolean add(AbstractPlugin p);

    public boolean add(AbstractPlugin p, boolean browsable);

    public boolean remove(String ctx);
    
    public boolean remove(AbstractPlugin p);

	public List<AbstractPlugin> getPlugins();
	
}
