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
package si.matjazcerkvenik.eventlogger.parsers;

import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpGetParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest dRequest) throws EventParserException {
        try {

            DEvent e = new DEvent();
            e.setId(DProps.increaseAndGetEventsReceivedCount());
            e.setRuntimeId(DProps.RUNTIME_ID);
            e.setTimestamp(System.currentTimeMillis());
            e.setHost(dRequest.getRemoteHost());
            e.setEndpoint(dRequest.getRequestUri());
            e.setEventSource(dRequest.getRemoteHost());
            e.setLogfile("unknown");
            e.setIdent("http.get");
            if (dRequest.getParameterMap().containsKey("ident")) {
                e.setIdent(dRequest.getParameterMap().get("ident"));
            }
            e.setPid("0");
            if (dRequest.getParameterMap().containsKey("pid")) {
                e.setPid(dRequest.getParameterMap().get("pid"));
            }
            if (dRequest.getParameterMap().containsKey("file")) {
                e.setLogfile(dRequest.getParameterMap().get("file"));
            }
            if (dRequest.getParameterMap().containsKey("tag")) {
                e.setTag(dRequest.getParameterMap().get("tag"));
            }
            if (dRequest.getParameterMap().containsKey("msg")) {
                e.setMessage(dRequest.getParameterMap().get("msg"));
            } else if (dRequest.getParameterMap().containsKey("message")) {
                e.setMessage(dRequest.getParameterMap().get("message"));
            } else {
                e.setMessage(null);
            }
            LogFactory.getLogger().trace(e.toString());
            DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), e.getHost(), e.getIdent()).inc();

            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                List<DEvent> eventList = new ArrayList<>();
                eventList.add(e);
                return eventList;
            }

            DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getMethod()).inc();
            LogFactory.getLogger().warn("HttpGetParser: parseRequest: message is empty; event will be ignored");

        } catch (Exception e) {
            LogFactory.getLogger().error("HttpGetParser: parseRequest: Exception: " + e.getMessage());
            throw new EventParserException("generic-get parser failed");
        }

        return null;
    }
}
