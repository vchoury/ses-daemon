package fr.vcy.coredaemon.httpd.plugins.impl;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.services.ProcessMonitor.ActiveProcess;

/**
 *
 * @author vchoury
 */
public class MonitorPlugin extends AbstractPlugin {
	
    public MonitorPlugin() throws IOException {
        super("/monitor", "Process Monitoring");
    }

    @Override
    public String usage() {
        return "GET /monitor : List des processus en cours \n"
                + "GET /monitor/jvm : Etat de la JVM";
    }

    @Override
    public HttpResponse doServe(HttpRequest request) throws Exception {
        String path = getLocalPathFromUri(request.getUri());
        if (request.getParms().containsKey("jvm") || path.contains("jvm")) {
            HtmlPage page = new HtmlPage();
            page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
            page.setBodyHeaderH3("JVM monitoring");

            ThreadMXBean th = ManagementFactory.getThreadMXBean();
            long[] deadlockids = th.findDeadlockedThreads();
            if (deadlockids != null) {
                page.addContent("Dead locks", Arrays.toString(deadlockids));
            }

            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            page.addContent("Heap Memory Usage", mem.getHeapMemoryUsage().toString());
            page.addContent("Non Heap Memory Usage", mem.getNonHeapMemoryUsage().toString());

            ThreadInfo[] thInfos = th.dumpAllThreads(true, true);
            page.addContent("Thread dumps", "");
            for (ThreadInfo thi : thInfos) {
                page.addContent(null, thi.toString());
            }
            
            return new HttpResponse(page.toString());
            
        } else {
            
            HtmlPage page = new HtmlPage();
            page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
            page.setBodyHeaderH3(getDescription());
            
            SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            StringBuilder sb = new StringBuilder();
            sb.append("<table border='1'><tr><th>Type</th><th>Desc</th><th>Start date</th></tr>");
            for (ActiveProcess process : getProcessMonitor().getProcesses()) {
                sb.append("<tr>");
                sb.append("<td>").append(process.getObj().getClass().getSimpleName()).append("</td>");
                sb.append("<td>").append(HtmlUtils.escape(process.getDesc())).append("</td>");
                sb.append("<td>").append(HtmlUtils.escape(form.format(process.getBeginDate()))).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            page.addHtmlContent("Running processes", sb.toString());
            
            page.addLinkListContent("JVM monitoring", getContext(), Collections.singleton("jvm").iterator());
            
            return new HttpResponse(page.toString());
        }
    }

}
