package fr.vcy.coredaemon.camel;

import java.io.File;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.processor.SendProcessor;
import org.apache.camel.processor.interceptor.DefaultChannel;
import org.apache.camel.processor.loadbalancer.LoadBalancerSupport;
import org.apache.camel.spi.Registry;

/**
 *
 * @author vchoury
 */
public class CustomLoadBalancer extends LoadBalancerSupport {
    
    public static void register(CamelContext ctx, String name) {
        Registry reg = ctx.getRegistry();
        if (reg instanceof PropertyPlaceholderDelegateRegistry) {
            Registry reg2 = ((PropertyPlaceholderDelegateRegistry) reg).getRegistry();
            if (reg2 instanceof SimpleRegistry) {
                ((SimpleRegistry) reg2).put(name, new CustomLoadBalancer());
            }
        }
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        Processor optim = null;
        Integer nbFilesMin = null;
        try {
            for (Processor proc : getProcessors()) {
                if (optim == null) {
                    optim = proc;
                }
                if (proc instanceof DefaultChannel) {
                    DefaultChannel chan = ((DefaultChannel) proc);
                    if (chan.getNextProcessor() instanceof SendProcessor) {
                        String uri = ((SendProcessor) chan.getNextProcessor()).getDestination().getEndpointUri();
                        uri = uri.replace("file://", "");
                        uri = uri.replace("%5C", "/");
                        File dest = new File(uri);
                        Integer i = countFiles(dest, 0);
                        if (nbFilesMin == null || nbFilesMin > i) {
                            nbFilesMin = i;
                            optim = proc;
                        }
                    }
                }
            }
            if (optim != null) {
                optim.process(exchange);
            }
        } catch (Exception ex) {
            exchange.setException(ex);
        }
        callback.done(true);
        return true;
    }

    public static int countFiles(File folder, int count) {
        int res = count;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    res++;
                } else {
                    res += countFiles(file, count);
                }
            }
        }
        return res;
    }
}
