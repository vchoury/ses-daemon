package fr.vcy.coredaemon.services;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;

/**
 *
 * @author vchoury
 */
public class DirectoryPoller extends Observable implements Runnable, Module {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(DirectoryPoller.class);
    public static final int DEFAULT_MS_LOOP = 2000;
    
    private File directory;
    private FilenameFilter filter = null;
    private boolean shutdownRequested = false;
    private int msLoop = DEFAULT_MS_LOOP;
    private Thread pollerThread;
    
    public DirectoryPoller(File dir) {
        this.directory = dir;
    }
    
	@Override
	public void init(CfgBase cfg) throws Exception {
	}

	@Override
	public void start(CfgBase cfg) throws Exception {
        pollerThread = new Thread(this);
        pollerThread.start();
        LOGGER.info("Polling every " + msLoop + "ms in directory " + directory.getAbsolutePath());
	}

	@Override
	public void stop(CfgBase cfg) throws Exception {
        shutdownRequested = true;
        this.deleteObservers();
        if (pollerThread != null && pollerThread.isAlive()) {
            pollerThread.interrupt();
        }
	}

	@Override
	public void reload(CfgBase cfg) throws Exception { 
        if (pollerThread != null && pollerThread.isAlive()) {
            pollerThread.interrupt();
        }
        start(cfg);
	}

    @Override
    public void run() {
        Thread.currentThread().setName("DirectoryPoller_(" + Thread.currentThread().getName() + ")");
        while (!shutdownRequested && !Thread.currentThread().isInterrupted()) {
            try {
                if (directory != null && directory.isDirectory()) {
                    for (String s : directory.list(filter)) {
                        setChanged();
                        notifyObservers(new File(directory, s));
                    }
                }
                Thread.sleep(msLoop);
            } catch (InterruptedException ex) {
                if (!shutdownRequested) {
                    LOGGER.warn("Poller sleep interrupted", ex);
                }
                break;
            }
        }
        LOGGER.info("Fin du thread de poll");
    }

    public File getDirectory() {
        return directory;
    }

    public FilenameFilter getFilter() {
        return filter;
    }

	public void setFilter(FilenameFilter filter) {
		this.filter = filter;
	}

}
