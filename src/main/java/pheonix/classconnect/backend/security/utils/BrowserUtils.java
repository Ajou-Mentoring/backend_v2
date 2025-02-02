package pheonix.classconnect.backend.security.utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

@Slf4j
public class BrowserUtils {
    /**
     * Renders an HTTP response that will cause the browser to POST the specified values to an url.
     * @param url the url where to perform the POST.
     * @param response the {@link HttpServletResponse}.
     * @param values the values to include in the POST.
     * @throws IOException thrown if an IO error occurs.
     */
    public static void postUsingBrowser(
            String url,
            HttpServletResponse response,
            Map<String, String> values) throws IOException {

        log.info("postUsingBrowser-> values={}", values);

        response.setContentType("text/html");
        Writer writer = response.getWriter();
        writer.write(
                "<html><head></head><body><form id='TheForm' action='" +
                        StringEscapeUtils.escapeHtml(url) +
                        "' method='POST'>"
        );

        for (String key : values.keySet()) {
            String encodedKey = StringEscapeUtils.escapeHtml(key);
            String encodedValue = StringEscapeUtils.escapeHtml(values.get(key));

            log.debug(
                    "postUsingBrowser-> key={}, value={}",
                    encodedKey, encodedValue);

            writer.write(
                    "<input type='hidden' id='" + encodedKey +
                            "' name='" + encodedKey +
                            "' value='" + encodedValue + "'/>");
        }

        log.debug("destination={}", StringEscapeUtils.escapeHtml(url));

        writer.write(
                "</form>" +
                        "<script type='text/javascript'>" +
                        "document.getElementById('TheForm').submit();" +
                        "</script></body></html>");

        writer.flush();

        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
    }

    public static void getUsingBrowser(
            String url,
            HttpServletResponse response,
            Map<String, String> values) throws IOException {

        response.setContentType("text/html");
        Writer writer = response.getWriter();

        StringBuilder sb = new StringBuilder();
        for (String key : values.keySet()) {
            String encodedKey = StringEscapeUtils.escapeHtml(key);
            String encodedValue = StringEscapeUtils.escapeHtml(values.get(key));

            if (sb.length() > 1) sb.append("&");
            log.debug("getUsingBrowser-> sb={}, encodedKey={}", sb, encodedKey);

            sb.append(encodedKey)
                    .append("=")
                    .append(encodedValue);
        }

        String uri = url;
        String param = sb.toString();
        if (param.length() > 0) uri += "?" + param;

        log.debug("getUsingBrowser-> uri={}", uri);

        writer.write(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta http-equiv='Refresh' content='0; url=" + uri + "'/>" +
                        "</head>" +
                        "</html>");

        writer.flush();

        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
    }
}

