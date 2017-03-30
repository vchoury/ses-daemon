package fr.vcy.coredaemon.httpd.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vchoury
 */
public class HtmlPage {
    
    public static final String DOCTYPE = "<!DOCTYPE html>";
    public static final String CHARSET = "<meta charset='utf-8' />";
    
    private static List<String> headCssUrls = new ArrayList<String>();
    private static List<String> headJsUrls = new ArrayList<String>();
    private static String headFaviconUrl;
    private String headTitle;
    private String headDescription;
    
    private static String bodyHeaderH1;
    private String bodyHeaderH2;
    private String bodyHeaderH3;
    
    // <title,href>
    private static Map<String,String> bodyNav = new HashMap<String,String>();
    
    private StringBuilder bodyContent = new StringBuilder();
    
    private static String bodyFooter;
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DOCTYPE).append("\n");
        sb.append("<html lang='fr'>").append("\n");
        sb.append("<head>").append("\n");
        sb.append("   ").append(CHARSET).append("\n");
        for (String headCssUrl : headCssUrls) {
            sb.append("   <link rel='stylesheet' type='text/css' href='").append(headCssUrl).append("' />\n");
        }
        for (String headJsUrl : headJsUrls) {
            sb.append("   <script type='text/javascript' src='").append(headJsUrl).append("' />\n");
        }
        if (headFaviconUrl != null) {
            sb.append("   <link rel='shortcut icon' href='").append(headFaviconUrl).append("' />\n");
        }
        if (headTitle != null) {
            sb.append("   <title>").append(HtmlUtils.escape(headTitle)).append("</title>\n");
        }
        if (headDescription != null) {
            sb.append("   <description>").append(HtmlUtils.escape(headDescription)).append("</description>\n");
        }
        sb.append("</head>").append("\n");
        sb.append("<body>").append("\n");
        sb.append("   <header>").append("\n");
        if (bodyHeaderH1 != null) {
            sb.append("      <h1>").append(HtmlUtils.escape(bodyHeaderH1)).append("</h1>\n");
        }
        if (bodyHeaderH2 != null) {
            sb.append("      <h2>").append(HtmlUtils.escape(bodyHeaderH2)).append("</h2>\n");
        }
        if (bodyHeaderH3 != null) {
            sb.append("      <h3>").append(HtmlUtils.escape(bodyHeaderH3)).append("</h3>\n");
        }
        sb.append("   </header>").append("\n");
        sb.append("   <nav><ul>").append("\n");
        for (String title : bodyNav.keySet()) {
            String href = bodyNav.get(title);
            sb.append("      <li><a href='").append(href).append("'>").append(HtmlUtils.escape(title)).append("</a>").append("</li>").append("\n");
        }
        sb.append("   </ul></nav>").append("\n");
        sb.append("   <div id='content'>").append("\n");
        sb.append(bodyContent).append("\n");
        sb.append("   </div>").append("\n");
        sb.append("   <footer>").append("\n");
        if (bodyFooter != null) {
            sb.append(bodyFooter).append("\n");
        }
        sb.append("   </footer>").append("\n");
        
        sb.append("</body>").append("\n");
        sb.append("</html>");
        return sb.toString();
    }
    
    public void addContent(String title, String content) {
        addHtmlContent(HtmlUtils.escape(title), HtmlUtils.escape(content));
    }
    
    public void addLinkListContent(String title, String context, Iterator<String> content) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        while (content.hasNext()) {
            String path = content.next();
            sb.append("<li><a href='").append(HtmlUtils.encodeUri(context + "/" + path)).append("'>").append(path).append("</a></li>");
        }
        sb.append("</ul>");
        addHtmlContent(HtmlUtils.escape(title), sb.toString());
    }
    
    public void addHtmlContent(String title, String content) {
        if (title != null) {
            bodyContent.append("<header>").append(title).append("</header>").append("\n");
        }
        bodyContent.append("<section>").append(content).append("</section>");
    }
    
    public void addRequestForm(String title, String url, String method, boolean pj, String paramKey, String paramValue) {
        addRequestForm(title, url, method, pj, paramKey, paramValue, null, null);
    }
    
    public void addRequestForm(String title, String url, String method, boolean pj, String paramKey1, String paramValue1, String paramKey2, String paramValue2) {
        addRequestForm(title, url, method, pj, paramKey1, paramValue1, paramKey2, paramValue2, null, null);
    }
    
    public void addRequestForm(String title, String url, String method, boolean pj, String paramKey1, String paramValue1, String paramKey2, String paramValue2, String paramKey3, String paramValue3) {
        if (title != null) {
            bodyContent.append("<header>").append(title).append("</header>").append("\n");
        }
        bodyContent.append("<section>");
        String m = method.toUpperCase();
        if (!"POST".equals(method) && !"GET".equals(method)) {
            m = "POST";
        }
        bodyContent.append("   <form action='").append(HtmlUtils.encodeUri(url)).append("' method='").append(m).append("' class='form' enctype='multipart/form-data'>");
        bodyContent.append("      <input type='hidden' name='method' id='method' readonly='readonly' value='").append(method).append("' />");
        if (paramKey1 != null) {
            bodyContent.append("      <label for='").append(paramKey1).append("'>").append(paramKey1).append(" : </label>  ");
            bodyContent.append("      <input type='text' name='").append(paramKey1).append("' id='").append(paramKey1).append("' ").append(" value='").append(paramValue1 == null ? "" : paramValue1).append("'/>");
        }
        if (paramKey2 != null) {
            bodyContent.append("      <label for='").append(paramKey2).append("'>").append(paramKey2).append(" : </label>  ");
            bodyContent.append("      <input type='text' name='").append(paramKey2).append("' id='").append(paramKey2).append("' ").append(" value='").append(paramValue2 == null ? "" : paramValue2).append("'/>");
        }
        if (paramKey3 != null) {
            bodyContent.append("      <label for='").append(paramKey3).append("'>").append(paramKey3).append(" : </label>  ");
            bodyContent.append("      <input type='text' name='").append(paramKey3).append("' id='").append(paramKey3).append("' ").append(" value='").append(paramValue3 == null ? "" : paramValue3).append("'/>");
        }
        if (pj) {
            bodyContent.append("      <input type='file' name='pj' id='pj' />  ");
        }
        bodyContent.append("      <input type='submit' value='").append(method).append("' />  ");
        bodyContent.append("   </form>");
        bodyContent.append("</section>");
    }
    
    public static void setHeadFaviconUrl(String headFaviconUrl) {
        HtmlPage.headFaviconUrl = headFaviconUrl;
    }

    public static void addCssUrl(String url) {
        HtmlPage.headCssUrls.add(url);
    }
    
    public static void addJsUrl(String url) {
        HtmlPage.headJsUrls.add(url);
    }
    
    public static void setBodyHeaderH1(String bodyHeaderH1) {
        HtmlPage.bodyHeaderH1 = bodyHeaderH1;
    }

    public static void addNav(String title, String url) {
        HtmlPage.bodyNav.put(title, url);
    }

    public static void removeNav(String title) {
        HtmlPage.bodyNav.remove(title);
    }

    public static void setBodyFooter(String bodyFooter) {
        HtmlPage.bodyFooter = bodyFooter;
    }

    public void setHeadTitle(String headTitle) {
        this.headTitle = headTitle;
    }

    public void setHeadDescription(String headDescription) {
        this.headDescription = headDescription;
    }

    public void setBodyHeaderH2(String bodyHeaderH2) {
        this.bodyHeaderH2 = bodyHeaderH2;
    }

    public void setBodyHeaderH3(String bodyHeaderH3) {
        this.bodyHeaderH3 = bodyHeaderH3;
    }

}
