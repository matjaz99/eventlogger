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

import si.matjazcerkvenik.eventlogger.db.EventQueue;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ReceiverServlet extends HttpServlet {

    private static final long serialVersionUID = 428459132243871691L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        DRequest dRequest = RequestProcessor.incomingRequest(req, DProps.increaseAndGetRequestsReceivedCount());

        if (Formatter.isNullOrEmpty(dRequest.getBody())) {
            DMetrics.eventlogger_requests_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "no content").inc();
            return;
        }

        DMetrics.eventlogger_http_requests_total.labels(req.getRemoteHost(), req.getMethod(), req.getRequestURI()).inc();
        DMetrics.eventlogger_http_requests_size_total.labels(req.getRemoteHost(), req.getMethod(), req.getRequestURI()).inc(req.getContentLength());


        EventQueue.getInstance().addIncomingRequest(dRequest);

    }

}
