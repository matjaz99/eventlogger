/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.eventlogger.webhooks;

import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RequestProcessor {

    public static DRequest incomingRequest(HttpServletRequest req, long requestId) throws IOException {

        String body = getReqBody(req);

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
        sb.append("contentType=").append(req.getContentType()).append("}");

        LogFactory.getLogger().info("incomingRequest: " + sb.toString());

        sb.deleteCharAt(sb.length()-1); // remove the last } and add some more parameters
        sb.append(", headersMap=[").append(getReqHeadersAsString(req)).append("], ");
        sb.append("parameterMap=[").append(getReqParamsAsString(req)).append("]\n");
        sb.append("body=").append(body);
        sb.append("}\n");

        LogFactory.getIncomingRequestsLog().info(sb.toString());

        DRequest m = new DRequest();
        m.setId(requestId);
        m.setRuntimeId(DProps.RUNTIME_ID);
        m.setTimestamp(System.currentTimeMillis());
        m.setContentLength(req.getContentLength());
        if (m.getContentLength() < 0) m.setContentLength(0);
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

        for (String s : parameterMap.keySet()) {
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
        for (String s : parameterMap.keySet()) {
            params += s + "=" + parameterMap.get(s)[0] + ", ";
        }
        return params;
    }

    private static String getReqBody(HttpServletRequest req) throws IOException {

        if (req.getMethod().equalsIgnoreCase("get")) {
            return req.getPathInfo() + " " + generateParamMap(req);
        }

        String body = ""; // FIXME use string builder
        String s = req.getReader().readLine();
        while (s != null) {
            body += s;
            s = req.getReader().readLine();
        }

        return body.trim();

    }

}
