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
package si.matjazcerkvenik.eventlogger.util;

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

    /**
     * Convert incoming HTTP request to DRequest.
     * @param request
     * @param requestId - counter
     * @return dRequest object
     * @throws IOException
     */
    public static DRequest incomingRequest(HttpServletRequest request, long requestId) throws IOException {

        String body = getReqBody(request);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("protocol=").append(request.getProtocol()).append(", ");
        sb.append("remoteAddr=").append(request.getRemoteAddr()).append(", ");
        sb.append("remoteHost=").append(request.getRemoteHost()).append(", ");
        sb.append("remotePort=").append(request.getRemotePort()).append(", ");
        sb.append("method=").append(request.getMethod()).append(", ");
        sb.append("requestURI=").append(request.getRequestURI()).append(", ");
        sb.append("scheme=").append(request.getScheme()).append(", ");
        sb.append("characterEncoding=").append(request.getCharacterEncoding()).append(", ");
        sb.append("contentLength=").append(request.getContentLength()).append(", ");
        sb.append("contentType=").append(request.getContentType()).append("}");

        LogFactory.getLogger().info("incomingRequest: " + sb.toString());

        sb.deleteCharAt(sb.length()-1); // remove the last } and add some more parameters
        sb.append(", headersMap=[").append(getReqHeadersAsString(request)).append("], ");
        sb.append("parameterMap=[").append(getReqParamsAsString(request)).append("]\n");
        sb.append("body=").append(body);
        sb.append("}\n");

        LogFactory.getIncomingRequestsLog().info(sb.toString());

        DRequest dRequest = new DRequest();
        dRequest.setId(requestId);
        dRequest.setRuntimeId(DProps.RUNTIME_ID);
        dRequest.setTimestamp(System.currentTimeMillis());
        dRequest.setContentLength(request.getContentLength());
        if (dRequest.getContentLength() < 0) dRequest.setContentLength(0);
        dRequest.setContentType(request.getContentType());
        dRequest.setMethod(request.getMethod());
        dRequest.setProtocol(request.getProtocol());
        dRequest.setRemoteHost(request.getRemoteHost());
        dRequest.setRemotePort(request.getRemotePort());
        dRequest.setRequestUri(request.getRequestURI());
        dRequest.setBody(body);
        dRequest.setHeaderMap(generateHeaderMap(request));
        dRequest.setParameterMap(generateParamMap(request));

        return dRequest;
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

    /**
     * If this is GET request, return URI parameters map.
     * If this is POST request than take whatever can be found in the body.
     * @param req
     * @return body text
     * @throws IOException
     */
    private static String getReqBody(HttpServletRequest req) throws IOException {

        if (req.getMethod().equalsIgnoreCase("get")) {
            return req.getPathInfo() + " " + generateParamMap(req);
        }

        StringBuilder sb = new StringBuilder();
        String s = req.getReader().readLine();
        while (s != null) {
            sb.append(s);
            s = req.getReader().readLine();
        }

        return sb.toString().trim();
    }

}
