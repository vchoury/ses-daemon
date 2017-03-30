package fr.vcy.coredaemon.httpd.plugins;

import fr.vcy.coredaemon.httpd.nano.NanoHTTPD;
import fr.vcy.coredaemon.httpd.plugins.impl.FileCounterPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.FilePlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.JmxPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.PropertiesPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.RequesterPlugin;
import fr.vcy.coredaemon.httpd.plugins.impl.SingleContentPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author vchoury
 */
public class PluginFactory {
    
    private PluginFactory() { }
    
    public abstract static class DelegatePluginFactory {

        public abstract AbstractPlugin createPlugin(String clazz, String ctx, String arg) throws InstantiationException;
        
    }
    
    private static List<DelegatePluginFactory> delegateFactories;
    
    public static void addDelegatePluginFactory(DelegatePluginFactory pf) {
        if (delegateFactories == null) {
            delegateFactories = new ArrayList<DelegatePluginFactory>();
        }
        delegateFactories.add(pf);
    }
    
    public static void removeDelegatePluginFactory(DelegatePluginFactory pf) {
        if (delegateFactories != null) {
            delegateFactories.remove(pf);
        }
    }

    public static AbstractPlugin createPlugin(String clazz, String ctx, String arg) throws InstantiationException {
        if (StringUtils.equalsIgnoreCase(clazz, FileCounterPlugin.class.getSimpleName())) {
            return createFileCounterPlugin(ctx, arg);
        } else if (StringUtils.equalsIgnoreCase(clazz, FilePlugin.class.getSimpleName())) {
            return createFilePlugin(ctx, arg);
        } else if (StringUtils.equalsIgnoreCase(clazz, RequesterPlugin.class.getSimpleName())) {
            return createHttpFormPlugin(ctx);
        } else if (StringUtils.equalsIgnoreCase(clazz, JmxPlugin.class.getSimpleName())) {
            return createJmxPlugin(ctx, arg);
        } else if (StringUtils.equalsIgnoreCase(clazz, PropertiesPlugin.class.getSimpleName())) {
            return createPropertiesPlugin(ctx);
        } else if (StringUtils.equalsIgnoreCase(clazz, SingleContentPlugin.class.getSimpleName())) {
            return createSingleContentPlugin(ctx, arg);
        } else if (delegateFactories != null) {
            for (DelegatePluginFactory factory : delegateFactories) {
                try {
                    return factory.createPlugin(clazz, ctx, arg);
                } catch (InstantiationException ex) { }
            }
        }
        throw new InstantiationException("Unknown plugin class : " + clazz);
    }

    private static AbstractPlugin createFileCounterPlugin(String ctx, String arg) throws InstantiationException {
        try {
            return new FileCounterPlugin(ctx, new File(arg));
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
    private static AbstractPlugin createFilePlugin(String ctx, String arg) throws InstantiationException {
        try {
            return new FilePlugin(ctx, new File(arg));
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
    private static AbstractPlugin createHttpFormPlugin(String ctx) throws InstantiationException {
        try {
            return new RequesterPlugin(ctx);
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
    private static AbstractPlugin createJmxPlugin(String ctx, String arg) throws InstantiationException {
        try {
            return new JmxPlugin(ctx, Integer.getInteger(arg));
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
    private static AbstractPlugin createPropertiesPlugin(String ctx) throws InstantiationException {
        try {
            return new PropertiesPlugin(ctx);
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
    private static AbstractPlugin createSingleContentPlugin(String ctx, String arg) throws InstantiationException {
        try {
            return new SingleContentPlugin(ctx, "", arg.getBytes(), NanoHTTPD.MIME_PLAINTEXT);
        } catch (Exception ex) {
            throw new InstantiationException(ex.toString());
        }
    }
    
}
