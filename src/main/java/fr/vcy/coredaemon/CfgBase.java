package fr.vcy.coredaemon;

import fr.vcy.coredaemon.utils.PropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 *
 * @author vchoury
 */
public class CfgBase {
    
    protected String[] mainArgs = null;
    protected String propPathBase = "PATH_BASE";
    protected String propName = "vcy.base.name";
    protected String propHost = "vcy.base.server.host";
    protected String propPort = "vcy.base.server.port";
    protected String propJmxPort = "vcy.base.jmx.port";
    protected String propAdminDir = "vcy.base.dir.admin";
    protected String propWorkDir = "vcy.base.dir.work";
    protected String propCamelDir = "vcy.base.camel.dir";
    protected String propCssFile = "vcy.base.html.css";

    public void setMainArgs(String[] mainArgs) {
        this.mainArgs = Arrays.copyOf(mainArgs, mainArgs.length);
    }
    
    public void loadTestProperties() throws IOException {
        PropertiesUtil.loadDefaultProperties("default-test.properties", true);
        if (System.getProperty(propPathBase) == null) {
            throw new IllegalArgumentException("La propriété " + propPathBase + " n'est pas renseignée.");
        }
    }
    
    public void loadProperties() throws IOException {
        if (mainArgs != null) {
            PropertiesUtil.loadArgsProperties(mainArgs, true);
        }
        PropertiesUtil.loadDefaultProperties("default.properties", false);
        if (System.getProperty(propPathBase) == null) {
            throw new IllegalArgumentException("La propriété " + propPathBase + " n'est pas renseignée.");
        }
    }
    
    public String getName() {
        return System.getProperty(propName);
    }
    
    public String getHost() {
        String h = System.getProperty(propHost);
        if (h == null || h.isEmpty()) {
            try {
                h = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) { }
        }
        return h;
    }
    
    public Integer getPort() {
        return Integer.parseInt(System.getProperty(propPort));
    }

    public Integer getJmxPort() {
        return Integer.parseInt(System.getProperty(propJmxPort));
    }
    
    public File getPathBase() {
        return new File(System.getProperty(propPathBase));
    }
    
    public File getAdminDir() {
        return new File(getPathBase(), System.getProperty(propAdminDir));
    }
    
    public File getWorkDir() {
        return new File(getPathBase(), System.getProperty(propWorkDir));
    }
    
    public File getCamelDir() {
        if (System.getProperty(propCamelDir) != null) {
            return new File(getPathBase(), System.getProperty(propCamelDir));
        }
        return null;
    }
    
    public File getCssFile() {
        if (System.getProperty(propCssFile) != null) {
            return new File(System.getProperty(propCssFile));
        }
        return null;
    }
    
}
