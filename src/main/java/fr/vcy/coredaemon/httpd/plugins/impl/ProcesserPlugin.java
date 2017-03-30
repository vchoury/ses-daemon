package fr.vcy.coredaemon.httpd.plugins.impl;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;

public abstract class ProcesserPlugin extends AbstractPlugin {

    public ProcesserPlugin() {
        super("/process", "Execute une commande");
    }
    
    @Override
    public String usage() {
        return "[POST] /[CHECKUP|RELOAD|SHUTDOWN] or ?do=[CHECKUP|RELOAD|SHUTDOWN]";
    }

    @Override
    public HttpResponse doServe(HttpRequest request) throws Exception {
        String path = getLocalPathFromUri(request.getUri());
        HttpResponse res = new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
        switch (request.getMethod()) {
            case GET: 
                HtmlPage page = new HtmlPage();
                page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
                page.setBodyHeaderH3(getDescription());
                page.addRequestForm("Process requester", getContext(), "POST", false, "do", null);
                res = new HttpResponse(page.toString());
                break;
            case POST:
            	if (!path.isEmpty()) {
                    if (doProcess(path)) {
                    	res = new HttpResponse("Command " + path + " proceed.");
                    } else {
                    	res = new HttpResponse(Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, "Command " + path + " fail.");
                    }
            	} else if (!request.getParms().isEmpty()) {
                    for (String value : request.getParms().values()) {
                        if (!Method.POST.toString().equalsIgnoreCase(value)) {
	                        if (doProcess(value)) {
	                        	res = new HttpResponse("Command " + value + " proceed.");
	                        } else {
	                        	res = new HttpResponse(Status.INTERNAL_ERROR, HttpHelper.MIME_PLAINTEXT, "Command " + value + " fail.");
	                        }
                        }
                    }
            	}
            	break;
        	default: 
        		break;
        }
        return res;
    }

	public abstract boolean doProcess(String path);

}
