package fr.vcy.coredaemon.httpd.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author vchoury
 */
public class HtmlUtils {

    private HtmlUtils() { }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    public static String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri += "/";
            } else if (tok.equals(" ")) {
                newUri += "%20";
            } else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

    public static String escape(String s) {
        String res = StringUtils.defaultString(s, "");
        res = StringEscapeUtils.escapeHtml4(res);
        return StringUtils.replaceEach(res, new String[]{"\n", "\t"}, new String[]{"<br/>", " - "});
    }
    
    public static String getBaseCss() {
        return "* { margin:0; padding:0; } header, footer, aside, nav, article { display: block; } "
                + "body { position:relative; font-family:Verdana,Arial,sans-serif; padding:60px; } "
                + "h1, h2, h3 { line-height:2.0em; } a { color:#555555; text-decoration: none; } "
                + "ul { list-style: none outside none; } "
                + "input { border: solid 1px #CCCCCC; margin: 2px; padding: 2px; } input[readonly] { border: none; background: none; } "
                + "nav { background: none repeat scroll 0 0 #F3F3F3; border: 1px solid #CCCCCC; position: fixed; top:0; left:0; right:0; } "
                + "nav ul li { float: left; margin:20px } "
                + "#content { padding: 25px; } #content header { padding: 15px; }";
    }
}
