package fr.vcy.coredaemon.httpd.plugins.impl;

import java.io.File;

import fr.vcy.coredaemon.httpd.HttpRequest;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.AbstractPlugin;
import fr.vcy.coredaemon.httpd.utils.HtmlPage;
import fr.vcy.coredaemon.httpd.utils.HtmlUtils;
import fr.vcy.coredaemon.httpd.utils.HttpHelper;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Method;
import fr.vcy.coredaemon.httpd.utils.HttpHelper.Status;
import fr.vcy.coredaemon.httpd.utils.Requester;
import fr.vcy.coredaemon.httpd.utils.Requester.HttpRequestException;

/**
 *
 * @author vchoury
 */
public class RequesterPlugin extends AbstractPlugin {
    
    public RequesterPlugin() {
        this ("/requester");
    }

    public RequesterPlugin(String ctx) {
        super(ctx, "Formulaire permettant de réaliser des requetes HTTP");
    }

    @Override
    public String usage() {
        return "GET " + getContext() + " : N'oubliez pas de renseigner une url et une methode.";
    }

    @Override
    public HttpResponse doServe(HttpRequest request) {
        if (request.getMethod() == Method.GET) {
            HtmlPage page = new HtmlPage();
            page.setBodyHeaderH2(getContext() + " - " + this.getClass().getSimpleName());
            page.setBodyHeaderH3(getDescription());
            page.addHtmlContent(null, printForm());
            return new HttpResponse(page.toString());
        } else if (request.getMethod() == Method.POST) {
            try {
                Requester requester = null;
                switch (Method.valueOf(request.getParms().get("method"))) {
                    case GET: requester = Requester.get(request.getParms().get("url"), true, request.getParms().get("param_key"), request.getParms().get("param_value"));
                        break;
                    case POST: requester = Requester.post(request.getParms().get("url"), true, request.getParms().get("param_key"), request.getParms().get("param_value"));
                        break;
                    case PUT: requester = Requester.put(request.getParms().get("url"), true, request.getParms().get("param_key"), request.getParms().get("param_value"));
                        break;
                    default: break;
                }
                if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                    for (String srcPath : request.getFiles().values()) {
                        File f = new File(srcPath);
                        if (f.exists()) {
                            requester.send(f);
                        }
                    }
                }
                return new HttpResponse(Status.valueOf(requester.message()), requester.contentType(), requester.body());
            } catch (HttpRequestException ex) {
                return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(ex.getMessage() + "\n\n -> Usage :\n" + usage()));
            }
        } else {
            return new HttpResponse(Status.BAD_REQUEST, HttpHelper.MIME_PLAINTEXT, HtmlUtils.escape(usage()));
        }

    }

    private String printForm() {
        return "<form action='' method='post' class='form' enctype='multipart/form-data'>"
                + "   <p class='url'>  "
                + "      <label for='url'>Url :</label>  "
                + "      <input type='text' name='url' id='url' />  "
                + "   </p>"
                + "   <p class='params'>  "
                + "      <label for='param_key'>Param_key :</label>  "
                + "      <input type='text' name='param_key' id='param_key' />  "
                + "      <label for='param_value'>Param_value :</label>  "
                + "      <input type='text' name='param_value' id='param_value' />  "
                + "   </p>"
                + "   <p class='pj'>  "
                + "      <label for='pj'>File :</label>  "
                + "      <input type='file' name='pj' id='pj' />  "
                + "   </p>"
                + "   <p class='method' >"
                + "      <input type='radio' name='method' value='GET'>GET"
                + "      <input type='radio' name='method' value='POST'>POST"
                + "      <input type='radio' name='method' value='PUT'>PUT"
                + "      <input type='radio' name='method' value='DELETE'>DELETE<br/>"
                + "   </p>"
                + "   <p class='submit'>  "
                + "      <input type='submit' value='Send' />  "
                + "   </p>  "
                + "</form>";
    }

}
