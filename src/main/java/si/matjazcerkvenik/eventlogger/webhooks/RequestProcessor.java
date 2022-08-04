package si.matjazcerkvenik.eventlogger.webhooks;

import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RequestProcessor {

    public static HttpRequest processIncomingRequest(HttpServletRequest req, long requestId) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("protocol=").append(req.getProtocol()).append(", ");
        sb.append("remoteAddr=").append(req.getRemoteAddr()).append(", ");
        sb.append("remoteHost=").append(req.getRemoteHost()).append(", ");
        sb.append("remotePort=").append(req.getRemotePort()).append(", ");
        sb.append("method=").append(req.getMethod()).append(", ");
        sb.append("requestURI=").append(req.getRequestURI()).append(", ");
        sb.append("scheme=").append(req.getScheme()).append(", ");
        sb.append("characterEncoding=").append(req.getCharacterEncoding()).append(", ");
        sb.append("contentLength=").append(req.getContentLength()).append(", ");
        sb.append("contentType=").append(req.getContentType());
        sb.append("}");

        LogFactory.getLogger().info("RequestProcessor: processIncomingRequest(): " + sb.toString());

        LogFactory.getLogger().debug("RequestProcessor: processIncomingRequest(): parameterMap: " + getReqParamsAsString(req));
        LogFactory.getLogger().debug("RequestProcessor: processIncomingRequest(): headers: " + getReqHeadersAsString(req));
        String body = getReqBody(req);
        LogFactory.getLogger().debug("RequestProcessor: processIncomingRequest(): body: " + body);

        HttpRequest m = new HttpRequest();
        m.setId(requestId);
        m.setRuntimeId(DProps.RUNTIME_ID);
        m.setTimestamp(System.currentTimeMillis());
        m.setContentLength(req.getContentLength());
        m.setContentType(req.getContentType());
        m.setMethod(req.getMethod());
        m.setProtocol(req.getProtocol());
        m.setRemoteHost(req.getRemoteHost());
        m.setRemotePort(req.getRemotePort());
        m.setRequestUri(req.getRequestURI());
        m.setBody(body);
        m.setHeaderMap(generateHeaderMap(req));
        m.setParameterMap(generateParamMap(req));

        return m;
    }


    private static Map<String, String> generateHeaderMap(HttpServletRequest req) {

        Map<String, String> m = new HashMap<String, String>();

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String val = req.getHeader(key);
            m.put(key, val);
        }
        return m;
    }

    private static Map<String, String> generateParamMap(HttpServletRequest req) {

        Map<String, String> m = new HashMap<String, String>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
            String s = it.next();
            m.put(s, parameterMap.get(s)[0]);
        }
        return m;
    }


    private static String getReqHeadersAsString(HttpServletRequest req) {

        String headers = "";
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            //headerNames.toString();
            String val = req.getHeader(key);
            headers += key + "=" + val + ", ";
        }
        return headers;

    }

    private static String getReqParamsAsString(HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        String params = "";
        for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
            String s = it.next();
            params += s + "=" + parameterMap.get(s)[0] + ", ";
        }
        return params;
    }

    private static String getReqBody(HttpServletRequest req) throws IOException {

        if (req.getMethod().equalsIgnoreCase("get")) {
            return req.getPathInfo() + " " + generateParamMap(req);
        }

        String body = "";
        String s = req.getReader().readLine();
        while (s != null) {
            body += s;
            s = req.getReader().readLine();
        }

        return body;

    }

}
