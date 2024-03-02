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
import java.util.Map;

public class HttpPostParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest dRequest) throws EventParserException {

        // guess what is the content of message:
        // (1) single json object - inside {}
        // (2) array of json objects - starting with [ and ending with ]
        //       - syntax: [{}, {},..{}]
        // (3) ndjson - contains }\n{ - this means at least 2 json objects
        //       - what if contentType=application/x-ndjson is misleading, and it is actually
        //         just single json object with newline delimiter - {}\n? - just use (1)
        // (4) json object containing array of objects: {[{}, {},..{}]}
        // (5) xml - starts with <xml
        // (6) plain text - anything that is not json or xml
        //
        // Avoid double-quotes, as they screw the json syntax if not properly escaped!

        try {

            if (dRequest.getContentType().equalsIgnoreCase("application/json")) {
                // TODO check if object or array and cut to pieces
                if (dRequest.getBody().startsWith("{") && dRequest.getBody().trim().endsWith("}")) {
                    return parseJsonObject(dRequest);
                }
                if (dRequest.getBody().startsWith("[") && dRequest.getBody().trim().endsWith("]")) {
                    return parseJsonArray(dRequest);
                }
                return parsePlainText(dRequest);
            } else if (dRequest.getContentType().equalsIgnoreCase("application/x-ndjson")) {
//                if (dRequest.getBody().contains("}\n{")) {
//                    // TODO check if ndjson and cut to pieces
//                    return parseNdjson(dRequest);
//                }
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
        return parsePlainText(dRequest);
    }

    public List<DEvent> parseJsonArray(DRequest dRequest) {
        try {

            List<DEvent> eventList = new ArrayList<>();

            String s = dRequest.getBody().trim();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Object[] obj = gson.fromJson(s, Object[].class);
//            System.out.println("parseJsonArray: " + obj.toString());

            for (int i = 0; i < obj.length; i++) {
//                System.out.println("parseJsonArray: obj: " + obj[i].toString());

                DEvent e = new DEvent();
                e.setRuntimeId(DProps.RUNTIME_ID);
                e.setTimestamp(System.currentTimeMillis());
                e.setEndpoint(dRequest.getRequestUri());
                e.setRemoteAddress(dRequest.getRemoteHost());

                // try to find hostname in json
                Gson gson2 = new Gson();
                String json = gson2.toJson(obj[i]);
                Map map = gson2.fromJson(json, Map.class);
//                System.out.println(">>> " + map.size());
                if (map.containsKey("hostname")) {
                    e.setEventSource(map.get("hostname").toString());
                    e.setHost(map.get("hostname").toString());
                } else if (map.containsKey("host")) {
                    e.setEventSource(map.get("host").toString());
                    e.setHost(map.get("host").toString());
                } else {
                    e.setEventSource(dRequest.getRemoteHost());
                    e.setHost(dRequest.getRemoteHost());
                }
//                e.setHost(dRequest.getRemoteHost());

                e.setMessage(obj[i].toString());
                if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                    // ignore event with empty message
                    DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "no content").inc();
                    return null;
                }

                if (dRequest.getParameterMap().containsKey("pid")) {
                    e.setPid(dRequest.getParameterMap().get("pid"));
                }
                if (dRequest.getParameterMap().containsKey("file")) {
                    e.setLogfile(dRequest.getParameterMap().get("file"));
                }

                if (dRequest.getHeaderMap().containsKey("tag")) {
                    // first check if tag is present in header
                    e.setTag(dRequest.getHeaderMap().get("tag"));
                } else {
                    // then check request parameters (uri)
                    if (dRequest.getParameterMap().containsKey("tag")) {
                        e.setTag(dRequest.getParameterMap().get("tag"));
                    } else {
                        // finally try searching content
                        if (map.containsKey("tag")) {
                            e.setTag(map.get("tag").toString());
                        }
                    }
                }

                if (dRequest.getParameterMap().containsKey("ident")) {
                    e.setIdent(dRequest.getParameterMap().get("ident"));
                } else {
                    // use tag, if ident is not found
                    e.setIdent(e.getTag());
                }

                e.setId(DProps.increaseAndGetEventsReceivedCount());
                LogFactory.getLogger().trace(e.toString());
                DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), e.getHost(), e.getIdent()).inc();

                eventList.add(e);

            }

            if (eventList.isEmpty()) LogFactory.getLogger().warn("HttpPostParser:parseJsonArray: eventList is empty!");

            return eventList;

        } catch (Exception e) {
            LogFactory.getLogger().error("HttpPostParser:parseJsonArray: Exception: " + e.getMessage());
        }
        return null;
    }

    public List<DEvent> parseNdjson(DRequest dRequest) {
        return null;
    }

    public List<DEvent> parsePlainText(DRequest dRequest) {
        try {

            DEvent e = new DEvent();
            e.setRuntimeId(DProps.RUNTIME_ID);
            e.setTimestamp(System.currentTimeMillis());
            e.setHost(dRequest.getRemoteHost());
            e.setEndpoint(dRequest.getRequestUri());
            e.setEventSource(dRequest.getRemoteHost());
            e.setMessage(dRequest.getBody());
            if (dRequest.getParameterMap().containsKey("ident")) {
                e.setIdent(dRequest.getParameterMap().get("ident"));
            } else {
                DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "missing ident").inc();
                return null;
            }
            if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "no content").inc();
                return null;
            }
            if (dRequest.getParameterMap().containsKey("pid")) {
                e.setPid(dRequest.getParameterMap().get("pid"));
            }
            if (dRequest.getParameterMap().containsKey("file")) {
                e.setLogfile(dRequest.getParameterMap().get("file"));
            }
            if (dRequest.getParameterMap().containsKey("tag")) {
                e.setTag(dRequest.getParameterMap().get("tag"));
            }
            e.setId(DProps.increaseAndGetEventsReceivedCount());
            LogFactory.getLogger().trace(e.toString());
            DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), e.getHost(), e.getIdent()).inc();

            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                List<DEvent> eventList = new ArrayList<>();
                eventList.add(e);
                return eventList;
            }

            LogFactory.getLogger().warn("HttpPostParser:parsePlainText: eventList is empty!");

        } catch (Exception e) {
            LogFactory.getLogger().error("HttpPostParser:parsePlainText: Exception: " + e.getMessage());
        }
        return null;
    }

}
