package fr.vcy.coredaemon.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vchoury
 */
public class PropertiesUtil {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class.getName());
    
    private PropertiesUtil() { }
    
    public static void loadArgsProperties(String[] args, boolean override) throws IOException {
        for (String arg : args) {
            File file = new File(arg);
            if (file.exists()) {
                Properties props = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props.load(fis);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
                LOGGER.info("Loading file properties : " + file.getAbsolutePath() + (override ? " [override]" : ""));
                for (String key : props.stringPropertyNames()) {
                    if (override || System.getProperty(key) == null) {
                        System.setProperty(key, props.getProperty(key));
                    }
                }
            }
        }
    }
    
    public static void loadDefaultProperties(String name, boolean override) throws IOException {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
        LOGGER.info("Loading default properties : " + name + (override ? " [override]" : ""));
        for (String key : props.stringPropertyNames()) {
            if (override || System.getProperty(key) == null) {
                System.setProperty(key, props.getProperty(key));
            }
        }
    }

    public static String printProperties(Properties props) {
        StringBuilder sb = new StringBuilder();
        TreeSet<Object> entries = new TreeSet<Object>();
        entries.addAll(props.keySet());//Va trier les clefs.
        for (Object key : entries) {
            sb.append("\n").append(printProperty(props, key));
        }
        return sb.toString();
    }
    
    public static String printProperty(Properties props, Object key) {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append("=").append(props.get(key));
        return sb.toString();
    }

}
