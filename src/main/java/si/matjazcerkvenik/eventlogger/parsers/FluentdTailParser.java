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

public class FluentdTailParser implements IEventParser {

    @Override
    public List<DEvent> parseRequest(DRequest dRequest) throws EventParserException {

        try {

            // TODO check if it is an array or just a json object

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            List<DEvent> elist = gson.fromJson(dRequest.getBody().trim(), new TypeToken<List<DEvent>>(){}.getType());

            String ident = "null";
            long now = System.currentTimeMillis();

            for (DEvent de : elist) {
                de.setId(DProps.increaseAndGetEventsReceivedCount());
                de.setRuntimeId(DProps.RUNTIME_ID);
                de.setTimestamp(now);
                de.setHost(dRequest.getRemoteHost());
                de.setEventSource(dRequest.getRemoteHost());
                de.setLogfile("unknown");
                de.setEndpoint(dRequest.getRequestUri());
                de.setIdent("http.post");
                de.setPid("0");
                if (dRequest.getParameterMap().containsKey("ident")) {
                    ident = dRequest.getParameterMap().get("ident");
                    de.setIdent(ident);
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
                LogFactory.getLogger().trace(de.toString());
//			System.out.println(de.toString());
            }

            if (elist != null && elist.size() > 0) {
                DMetrics.eventlogger_events_total.labels(dRequest.getRemoteHost(), dRequest.getRemoteHost(), ident).inc(elist.size());
                return elist;
            }

            DMetrics.eventlogger_events_ignored_total.labels(dRequest.getRemoteHost(), dRequest.getMethod()).inc();
            LogFactory.getLogger().warn("HttpWebhook: doPost: message is empty; event will be ignored");
            return null;

        } catch (Exception e) {
            LogFactory.getLogger().error("FluentdTailParser: parseRequest: Exception: " + e.getMessage());
            throw new EventParserException("fluentd-tail parser failed");
        }


    }
}
