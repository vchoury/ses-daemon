package fr.vcy.coredaemon;

/**
 *
 * @author vchoury
 */
public interface AppMBean {
    
    /**
     * Base command : CHECKUP ; RELOAD ; SHUTDOWN
     */
    public boolean process(String cmd);
    
}
