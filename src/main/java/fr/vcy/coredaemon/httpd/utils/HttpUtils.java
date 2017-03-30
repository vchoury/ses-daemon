package fr.vcy.coredaemon.httpd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;

/**
 * @deprecated HttpRequest est plus complete!
 * @author vchoury
 */
public class HttpUtils {

    private HttpUtils() { }
    
    public static class SimpleHttpResponse {

        protected int code;
        protected String contentType;
        protected String content;

        public int getCode() {
            return code;
        }

        public String getContentType() {
            return contentType;
        }

        public String getContent() {
            return content;
        }
    }

    public static SimpleHttpResponse sendRequest(URL url, String requestMethod, String contentType, InputStream content, File dest) throws IOException {
        SimpleHttpResponse response = new SimpleHttpResponse();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setDoInput(true);            
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (content != null) {
                connection.setDoOutput(true);
                IOUtils.copy(content, connection.getOutputStream());
                IOUtils.closeQuietly(connection.getOutputStream());
            }
            //Get Response
            response.code = connection.getResponseCode();
            response.contentType = connection.getContentType();
            response.content = getResponseContent(connection, dest);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static SimpleHttpResponse sendRequest(URL url, String requestMethod, String contentType, InputStream content) throws IOException {
        SimpleHttpResponse response = new SimpleHttpResponse();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (content != null) {
                connection.setDoOutput(true);
                IOUtils.copy(content, connection.getOutputStream());
                IOUtils.closeQuietly(connection.getOutputStream());
            }
            //Get Response
            response.code = connection.getResponseCode();
            response.contentType = connection.getContentType();
            response.content = getResponseContent(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    private static String getResponseContent(HttpURLConnection connection, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            IOUtils.copy(connection.getInputStream(), fos);
            return file.getAbsolutePath();
        } catch (IOException ex) {
            IOUtils.closeQuietly(fos);
            return getResponseContent(connection);
        }
    }

    private static String getResponseContent(HttpURLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        try {
            try {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            } catch (IOException ex) {
                if (connection.getErrorStream() != null) {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                } else {
                    sb.append(ex.toString());
                }
            }
            String line;
            while (in != null && (line = in.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\r');
                }
                sb.append(line);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return sb.toString();
    }
}
