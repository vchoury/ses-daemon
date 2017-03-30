package fr.vcy.coredaemon.httpd.nano;

import java.util.Map;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpRequest.RequestVisitor;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.nano.NanoHTTPD.Response.Status;

public class ServerProxy extends NanoHTTPD {
	
	private RequestVisitor visitor;

	public ServerProxy(String hostname, int port, RequestVisitor visitor) {
		super(hostname, port);
		this.visitor = visitor;
	}

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
    	HttpRequest request = new HttpRequest(uri, method.toString(), header, parms, files);
    	visitor.visit(request);
    	HttpResponse res = request.getResponse();
    	return new Response(Status.valueOf(res.getStatus().toString()), res.getMimeType(), res.getData());
    }
	
    
}
