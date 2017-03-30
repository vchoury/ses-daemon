package fr.vcy.coredaemon.services;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vcy.coredaemon.CfgBase;
import fr.vcy.coredaemon.Module;

/**
 *
 * @author vchoury
 */
public class ProcessMonitor implements Module {

    public final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class.getName());
    private long loop = 90000;
    private Map<Object, ActiveProcess> processes = Collections.synchronizedMap(new HashMap<Object, ActiveProcess>());
    private AtomicLong sequence = new AtomicLong(1);
    private Thread timeoutThread;
    private boolean shutdownRequested = false;

	@Override
	public void init(CfgBase cfg) throws Exception { 
		processes.clear();
	}

	@Override
	public void start(CfgBase cfg) throws Exception { 
		shutdownRequested = false;
        startTimeoutThread();
    }

	@Override
	public void stop(CfgBase cfg) {
		synchronized(this) {
	        shutdownRequested = true;
	        if (timeoutThread != null && timeoutThread.isAlive()) {
	            timeoutThread.interrupt();
	        }
		}
	}

	@Override
	public void reload(CfgBase cfg) throws Exception {
		stop(cfg);
		init(cfg);
		start(cfg);
	}

    public List<ActiveProcess> getProcesses() {
        return new ArrayList<ActiveProcess>(processes.values());
    }

    public long getLoop() {
		return loop;
	}

	public void setLoop(long loop) {
		this.loop = loop;
	}

	public synchronized void startTimeoutThread() {
        if (timeoutThread == null || !timeoutThread.isAlive()) {
            timeoutThread = new Thread(new Runnable() {

                public void run() {
                    Thread.currentThread().setName("Monitor_(" + Thread.currentThread().getName() + ")");
                    LOGGER.info("Demarrage du thread de monitoring. Vérification du timeout des processus toutes les " + loop + "ms.");
                    while (!shutdownRequested && !Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(loop);
                            long tick = Calendar.getInstance().getTimeInMillis();
                            for (Object processer : processes.keySet().toArray()) {
                            	ActiveProcess process = processes.get(processer);
                                if (!process.getThread().isAlive()) {
                                    LOGGER.warn("Le process n'a pas été terminé correctement !");
                                    processes.remove(processer);
                                } else if (process.getTimeout() > 0 && tick - process.getBeginDate().getTime() > process.getTimeout()) {
                                    String msg = "Le thread " + process.getThread().getName() + " (" + process.getDesc() + ")"
                                            + " tourne depuis le " + DateFormat.getDateTimeInstance().format(process.getBeginDate()) + "."
                                            + " Le timeout a été fixé à " + process.getTimeout() + "ms. Tentative d'interruption.";
                                    LOGGER.warn(msg);
                                    msg += "\n\n Stacktrace : ";
                                    for (StackTraceElement el : process.getThread().getStackTrace()) {
                                        msg += "\n" + el.toString();
                                    }
                                    process.getThread().interrupt();
                                    MailService.sendMailToAdminQuietly("Timeout : thread interrompu", msg, null);
                                    processes.remove(processer);
                                }
                            }
                        } catch (InterruptedException ex) {
                            if (!shutdownRequested) {
                                LOGGER.warn("Monitor sleep interrupted", ex);
                            }
                            break;
                        }
                    }
                    LOGGER.info("Fin du thread de monitoring");
                }
            });
            timeoutThread.start();
        }
    }
    
	public void startMonitoring(Object processer, long timeout) {
		if (processes.containsKey(processer)) {
            LOGGER.warn("Debut de monitoring demandé pour un process existant : " + processer);
		} else {
	        ActiveProcess p = new ActiveProcess(sequence.getAndIncrement(), Thread.currentThread(), 
	        		processer, processer.toString(), Calendar.getInstance().getTime(), timeout);
	        processes.put(processer, p);
	        if (timeout < loop) {
	        	loop = timeout;
	        }
            startTimeoutThread();
		}
	}
	
	public void stopMonitoring(Object processer) {
		if (processes.containsKey(processer)) {
			processes.remove(processer);
		} else {
            LOGGER.warn("Fin de monitoring demandé pour un process inexistant : " + processer);
		}
	}
	
    public class ActiveProcess {

        private long id;
        private Thread thread;
        private Object obj;
        private String desc;
        private Date beginDate;
        private long timeout;

        public ActiveProcess(long id, Thread thread, Object obj, String desc, Date beginDate, long timeout) {
            this.id = id;
            this.thread = thread;
            this.obj = obj;
            this.desc = desc;
            this.beginDate = beginDate;
            this.timeout = timeout;
        }

        public long getTimeout() {
            return timeout;
        }

        public Thread getThread() {
            return thread;
        }

        public Date getBeginDate() {
            return beginDate;
        }

        public String getDesc() {
            return desc;
        }

        public long getId() {
            return id;
        }

        public Object getObj() {
            return obj;
        }
    }

}
