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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class FluentdSyslogParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest request) throws EventParserException {
        try {

            List<DEvent> eventList = new ArrayList<>();

            if (request.getContentType().equalsIgnoreCase("application/x-ndjson")) {
                // this is a ndjson (objects separated by \n): {}{}{}
                String body = request.getBody().replace("}{", "}\n{");
                String[] msgArray = body.split("\n");

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                long now = System.currentTimeMillis();

                for (int i = 0; i < msgArray.length; i++) {
                    DEvent e = gson.fromJson(msgArray[i].trim(), DEvent.class);
                    e.setRuntimeId(DProps.RUNTIME_ID);
                    e.setTimestamp(now);
                    e.setRemoteAddress(request.getRemoteHost());
                    e.setEventSource(request.getRemoteHost());
                    e.setLogfile("messages");
                    e.setEndpoint(request.getRequestUri());
                    if (e.getHost() == null) e.setHost(request.getRemoteHost());
                    if (e.getIdent() == null || e.getIdent().trim().length() == 0) {
                        DMetrics.eventlogger_events_ignored_total.labels(request.getRemoteHost(), request.getRequestUri(), "missing ident").inc();
                        continue;
                    } else if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                        DMetrics.eventlogger_events_ignored_total.labels(request.getRemoteHost(), request.getRequestUri(), "no content").inc();
                        continue;
                    }
                    e.setId(DProps.increaseAndGetEventsReceivedCount());
                    eventList.add(e);
                    LogFactory.getLogger().trace(e.toString());
                    DMetrics.eventlogger_events_total.labels(request.getRemoteHost(), e.getHost(), e.getIdent()).inc();
                }
            }
            if (request.getContentType().equalsIgnoreCase("application/json")) {
                // this is a json (array of objects): [{},{},{}]
                String body = request.getBody().substring(1, request.getBody().length() - 1);
                body = body.replace("},{", "},%#%#%#%{");
                String[] msgArray = body.split(",%#%#%#%"); // FIXME },{ may occur inside json object!

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                long now = System.currentTimeMillis();

                for (int i = 0; i < msgArray.length; i++) {
                    DEvent e = gson.fromJson(msgArray[i].trim(), DEvent.class);
                    e.setRuntimeId(DProps.RUNTIME_ID);
                    e.setTimestamp(now);
                    e.setRemoteAddress(request.getRemoteHost());
                    e.setEventSource(request.getRemoteHost());
                    e.setLogfile("messages");
                    e.setEndpoint(request.getRequestUri());
                    if (e.getHost() == null) e.setHost(request.getRemoteHost());
                    if (e.getIdent() == null || e.getIdent().trim().length() == 0) {
                        DMetrics.eventlogger_events_ignored_total.labels(request.getRemoteHost(), request.getRequestUri(), "missing ident").inc();
                        continue;
                    } else if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                        DMetrics.eventlogger_events_ignored_total.labels(request.getRemoteHost(), request.getRequestUri(), "no content").inc();
                        continue;
                    }
                    e.setId(DProps.increaseAndGetEventsReceivedCount());
                    eventList.add(e);
                    LogFactory.getLogger().trace(e.toString());
                    DMetrics.eventlogger_events_total.labels(request.getRemoteHost(), e.getHost(), e.getIdent()).inc();
                }
            }

            if (eventList != null && eventList.size() > 0) {
                return eventList;
            }

            LogFactory.getLogger().warn("FluentdSyslogParser: eventList is empty!");
            return null;

        } catch (Exception e) {
            LogFactory.getLogger().error("FluentdSyslogParser: Exception: " + e.getMessage());
            throw new EventParserException("fluentd-syslog parser failed");
        }
    }
}
