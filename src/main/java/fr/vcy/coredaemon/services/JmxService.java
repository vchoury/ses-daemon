package fr.vcy.coredaemon.services;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.AppMBean;
import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;
import fr.vcy.coredaemon.camel.CamelModule;

public class JmxService implements Module {

    public static final Logger LOGGER = LoggerFactory.getLogger(CamelModule.class.getName());
    
	private String mbeanName;
	private AppMBean app;

	public JmxService(AppMBean mbean) {
		this.app = mbean;
		mbeanName = mbean.getClass().getPackage().getName() + ":type=Management";
	}
	
	@Override
	public void init(CfgBase cfg) throws Exception {
		if (System.getProperty("com.sun.management.jmxremote") == null) {
			LOGGER.warn("com.sun.management.jmxremote is not defined");
		}
	}

	@Override
	public void start(CfgBase cfg) throws Exception {
        if (System.getProperty("com.sun.management.jmxremote") != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            StandardMBean mbean = new StandardMBean(app, AppMBean.class);
            mbs.registerMBean(mbean, new ObjectName(mbeanName));
        }
	}

	@Override
	public void stop(CfgBase cfg) {
        if (System.getProperty("com.sun.management.jmxremote") != null) {
	        try {
	            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	            mbs.unregisterMBean(new ObjectName(mbeanName));
			} catch (MBeanRegistrationException e) {
				LOGGER.error("Error while stopping JMX module", e);
			} catch (InstanceNotFoundException e) {
				LOGGER.error("Error while stopping JMX module", e);
			} catch (MalformedObjectNameException e) {
				LOGGER.error("Error while stopping JMX module", e);
			}
        }
	}

	@Override
	public void reload(CfgBase cfg) throws Exception {
		stop(cfg);
		init(cfg);
		start(cfg);
	}

}
