package fr.vcy.coredaemon.httpd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;

public class HttpRequest {
	
	public interface RequestVisitor {

		public void visit(HttpRequest request);
		
	}

    public static final long DEFAULT_TIMEOUT_MS = 10000;
	private String uri;
	private Method method;
	private Map<String, String> header = new HashMap<String, String>();
	private Map<String, String> parms = new HashMap<String, String>();
	private Map<String, String> files = new HashMap<String, String>();

	private HttpResponse response;
	
	private long timeout;

	public HttpRequest(String uri, String method) {
		this(uri, method, null, null, null);
	}

	public HttpRequest(String uri, String method, Map<String, String> header,
			Map<String, String> parms, Map<String, String> files) {
		this(uri, method, header, parms, files, DEFAULT_TIMEOUT_MS);
	}
	
	public HttpRequest(String uri, String method, Map<String, String> header,
			Map<String, String> parms, Map<String, String> files, long timeout) {
		this.uri = uri;
		this.method = Method.valueOf(method);
		if (header != null) {
			this.header = header;
		}
		if (parms != null) {
			this.parms = parms;
		}
		if (files != null) {
			this.files = files;
		}
		this.timeout = timeout;
	}
	
	public void accept(RequestVisitor visitor) {
		visitor.visit(this);
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Map<String, String> getHeader() {
		return header;
	}
	public void addHeader(String key, String value) {
		this.header.put(key, value);
	}
	public Map<String, String> getParms() {
		return parms;
	}
	public void addParms(String key, String value) {
		this.parms.put(key, value);
	}
	public Map<String, String> getFiles() {
		return files;
	}
	public void addFiles(String key, String value) {
		this.files.put(key, value);
	}

    public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
     * Print request details
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getMethod()).append(" '").append(this.getUri()).append("' ");
        if (!this.getHeader().isEmpty()) {
            sb.append("\n  HEADER:");
        }
        Iterator<String> e = this.getHeader().keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            sb.append(" '").append(value).append("' = '").append(this.getHeader().get(value)).append("' ;");
        }
        if (!this.getParms().isEmpty()) {
            sb.append("\n  PARAMS:");
        }
        e = this.getParms().keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            sb.append(" '").append(value).append("' = '").append(this.getParms().get(value)).append("' ;");
        }
        if (!this.getFiles().isEmpty()) {
            sb.append("\n  FILES:");
        }
        e = this.getFiles().keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            sb.append(" '").append(value).append("' = '").append(this.getFiles().get(value)).append("' ;");
        }
        if (response != null) {
            sb.append("\n  " + response.toString());
        }
        return sb.toString();
    }
	
}
