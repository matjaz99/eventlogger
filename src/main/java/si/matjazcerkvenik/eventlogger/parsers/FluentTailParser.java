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
import com.google.gson.reflect.TypeToken;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.List;

public class FluentTailParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest dRequest) throws EventParserException {

        try {

            // TODO check if it is an array or just a json object

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            List<DEvent> eventList = gson.fromJson(dRequest.getBody().trim(), new TypeToken<List<DEvent>>(){}.getType());

            long now = System.currentTimeMillis();

            for (DEvent de : eventList) {
                de.setRuntimeId(DProps.RUNTIME_ID);
                de.setTimestamp(now);
                de.setHost(dRequest.getRemoteHost());
                de.setRemoteAddress(dRequest.getRemoteHost());
                de.setEventSource(dRequest.getRemoteHost());
                de.setEndpoint(dRequest.getRequestUri());
                if (dRequest.getParameterMap().containsKey("ident")) {
                    de.setIdent(dRequest.getParameterMap().get("ident"));
                } else {
                    DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "missing ident").inc();
                    eventList.remove(de);
                    continue;
                }
                if (de.getMessage() == null || de.getMessage().trim().length() == 0) {
                    DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getRequestUri(), "no content").inc();
                    eventList.remove(de);
                    continue;
                }
                if (dRequest.getParameterMap().containsKey("pid")) {
                    de.setPid(dRequest.getParameterMap().get("pid"));
                }
                if (dRequest.getParameterMap().containsKey("file")) {
                    de.setLogfile(dRequest.getParameterMap().get("file"));
                }
                if (dRequest.getParameterMap().containsKey("tag")) {
                    de.setTag(dRequest.getParameterMap().get("tag"));
                }
                de.setId(DProps.increaseAndGetEventsReceivedCount());
                LogFactory.getLogger().trace(de.toString());
                DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), de.getHost(), de.getIdent()).inc();
            }

            if (eventList != null && eventList.size() > 0) {
                return eventList;
            }

            LogFactory.getLogger().warn("FluentTailParser: eventList is empty!");
            return null;

        } catch (Exception e) {
            LogFactory.getLogger().error("FluentTailParser: Exception: " + e.getMessage());
            throw new EventParserException("fluent-tail parser failed");
        }


    }
}
