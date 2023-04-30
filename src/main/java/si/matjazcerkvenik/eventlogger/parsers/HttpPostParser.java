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

public class HttpPostParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest dRequest) throws EventParserException {
        try {

            if (dRequest.getContentType().equalsIgnoreCase("application/json")) {
                // TODO check if object or array and cut to pieces
                return parsePlainText(dRequest);
            } else if (dRequest.getContentType().equalsIgnoreCase("application/x-ndjson")) {
                // TODO check if ndjson and cut to pieces
                return parsePlainText(dRequest);
            } else {
                // text/plain or application/xml or anything else
                return parsePlainText(dRequest);
            }

        } catch (Exception e) {
            LogFactory.getLogger().error("HttpPostParser: parseRequest: Exception: " + e.getMessage());
            throw new EventParserException("generic-post parser failed");
        }

    }

    public List<DEvent> parseJsonObject(DRequest dRequest) {
        return null;
    }

    public List<DEvent> parseJsonArray(DRequest dRequest) {
        return null;
    }

    public List<DEvent> parseNdjson(DRequest dRequest) {
        return null;
    }

    public List<DEvent> parsePlainText(DRequest dRequest) {
        try {

            DEvent e = new DEvent();
            e.setId(DProps.increaseAndGetEventsReceivedCount());
            e.setRuntimeId(DProps.RUNTIME_ID);
            e.setTimestamp(System.currentTimeMillis());
            e.setHost(dRequest.getRemoteHost());
            e.setEndpoint(dRequest.getRequestUri());
            e.setEventSource(dRequest.getRemoteHost());
            e.setLogfile("unknown");
            e.setIdent("http.post.plain-text");
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
            e.setMessage(dRequest.getBody());
            LogFactory.getLogger().trace(e.toString());
            DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), e.getHost(), e.getIdent()).inc();

            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                List<DEvent> eventList = new ArrayList<>();
                eventList.add(e);
                return eventList;
            }

            DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getMethod()).inc();
            LogFactory.getLogger().warn("HttpPostParser: parsePlainText: message is empty; event will be ignored");

        } catch (Exception e) {
            LogFactory.getLogger().error("HttpPostParser: parsePlainText: Exception: " + e.getMessage());
        }
        return null;
    }

}
