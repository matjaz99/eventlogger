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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import io.prometheus.client.Counter;
import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.eventlogger.model.DAlarmSeverity;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.model.config.DRule;
import si.matjazcerkvenik.eventlogger.model.config.DRulesGroup;
import si.matjazcerkvenik.eventlogger.util.AlarmMananger;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiverServlet extends HttpServlet {

    private static final long serialVersionUID = 428459132243871691L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        DRequest m = RequestProcessor.processIncomingRequest(req, DProps.requestsReceivedCount++);

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        iDataManager.addHttpRequest(m);
        DMetrics.eventlogger_http_requests_total.labels(m.getRemoteHost(), m.getMethod(), req.getRequestURI()).inc();
        DMetrics.eventlogger_http_requests_size_total.labels(m.getRemoteHost(), m.getMethod(), req.getRequestURI()).inc(m.getContentLength());

        List<DEvent> eventList = null;

        if (m.getRequestUri().equalsIgnoreCase("/eventlogger/event/fluentd-syslog")) {
            eventList = processFluentdSyslogRequest(m);
        } else if (m.getRequestUri().equalsIgnoreCase("/eventlogger/event/http")
                && m.getMethod().equalsIgnoreCase("GET")) {
            eventList = processHttpGetRequest(m);
        } else if (m.getRequestUri().equalsIgnoreCase("/eventlogger/event/http")
                && m.getMethod().equalsIgnoreCase("POST")) {
            eventList = processHttpPostRequest(m, iDataManager);
        } else {
            LogFactory.getLogger().warn("ReceiverServlet: doPost: endpoint not supported: " + m.getRequestUri());
        }

        if (eventList != null) {
            for (DEvent e : eventList) {
                evaluateRules(e);
            }
        }

        iDataManager.addEvents(eventList);
        DataManagerFactory.getInstance().returnClient(iDataManager);

    }
    
    
    private List<DEvent> processFluentdSyslogRequest(DRequest m) {

        try {

            // process body
            List<DEvent> eventList = new ArrayList<>();

            if (m.getContentType().equalsIgnoreCase("application/x-ndjson")) {
                // this is a ndjson (objects separated by \n): {}{}{}
                String body = m.getBody().replace("}{", "}\n{");
                String[] msgArray = body.split("\n");

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                long now = System.currentTimeMillis();

                for (int i = 0; i < msgArray.length; i++) {
                    DEvent e = gson.fromJson(msgArray[i].trim(), DEvent.class);
                    e.setId(DProps.eventsReceivedCount++);
                    e.setRuntimeId(DProps.RUNTIME_ID);
                    e.setTimestamp(now);
                    e.setEventSource("fluentd.syslog");
                    e.setEndpoint(m.getRequestUri());
                    eventList.add(e);
                    if (e.getHost() == null) e.setHost(m.getRemoteHost());
                    if (e.getIdent() == null) e.setIdent("unknown");
                    LogFactory.getLogger().trace(e.toString());
                    DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), e.getHost(), e.getIdent()).inc();

                }
            }
            if (m.getContentType().equalsIgnoreCase("application/json")) {
                // this is a json (array of objects): [{},{},{}]
            }

            return eventList;

        } catch (Exception e) {
            LogFactory.getLogger().warn("ReceiverServlet: processFluentdSyslogRequest: Exception: " + e.getMessage());
        }

        return null;

    }

    private List<DEvent> processHttpGetRequest(DRequest m) {

        try {

            // process body
            DEvent e = new DEvent();
            e.setId(DProps.eventsReceivedCount++);
            e.setRuntimeId(DProps.RUNTIME_ID);
            e.setTimestamp(System.currentTimeMillis());
            e.setHost(m.getRemoteHost());
            e.setIdent("eventlogger.http.get");
            e.setPid("0");
            if (m.getParameterMap().containsKey("ident")) {
                e.setIdent(m.getParameterMap().get("ident"));
            }
            if (m.getParameterMap().containsKey("pid")) {
                e.setPid(m.getParameterMap().get("pid"));
            }
            if (m.getParameterMap().containsKey("tag")) {
                e.setTag(m.getParameterMap().get("tag"));
            }
            if (m.getParameterMap().containsKey("msg")) {
                e.setMessage(m.getParameterMap().get("msg"));
            } else if (m.getParameterMap().containsKey("message")) {
                e.setMessage(m.getParameterMap().get("message"));
            } else {
                e.setMessage(null);
            }
            e.setEndpoint(m.getRequestUri());
            e.setEventSource("eventlogger.http.get");
            LogFactory.getLogger().trace(e.toString());
            DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), e.getHost(), e.getIdent()).inc();

            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                List<DEvent> eventList = new ArrayList<>();
                eventList.add(e);
                return eventList;
            }

            DMetrics.eventlogger_events_ignored_total.labels(m.getRemoteHost(), m.getMethod()).inc();
            LogFactory.getLogger().warn("ReceiverServlet: processHttpRequest: message is empty; event will be ignored");

        } catch (Exception e) {
            LogFactory.getLogger().warn("ReceiverServlet: processHttpRequest: Exception: " + e.getMessage());
        }

        return null;

    }

    private List<DEvent> processHttpPostRequest(DRequest m, IDataManager iDataManager) {

        try {

            if (m.getContentType().equalsIgnoreCase("application/json")) {
                return processApplicationJson(m);
            }
            if (m.getContentType().equalsIgnoreCase("text/plain")) {
            }
            if (m.getContentType().equalsIgnoreCase("application/xml")) {
            }



        } catch (Exception e) {
            LogFactory.getLogger().warn("HttpWebhook: doPost: Exception: " + e.getMessage());
        }

        return null;

    }

    private List<DEvent> processApplicationJson(DRequest m) {
        // process body
        // TODO check if it is an array or just a json object

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<DEvent> elist = gson.fromJson(m.getBody().trim(), new TypeToken<List<DEvent>>(){}.getType());

        String ident = "null";
        long now = System.currentTimeMillis();

        for (DEvent de : elist) {
            de.setId(DProps.eventsReceivedCount++);
            de.setRuntimeId(DProps.RUNTIME_ID);
            de.setTimestamp(now);
            de.setHost(m.getRemoteHost());
            de.setEventSource(m.getRemoteHost());
            de.setEndpoint(m.getRequestUri());
            de.setIdent("eventlogger.http.post");
            de.setPid("0");
            if (m.getParameterMap().containsKey("ident")) {
                ident = m.getParameterMap().get("ident");
                de.setIdent(ident);
            }
            if (m.getParameterMap().containsKey("pid")) {
                de.setPid(m.getParameterMap().get("pid"));
            }
            if (m.getParameterMap().containsKey("tag")) {
                de.setTag(m.getParameterMap().get("tag"));
            }
            LogFactory.getLogger().trace(de.toString());
//			System.out.println(de.toString());
        }

        if (elist != null && elist.size() > 0) {
            DMetrics.eventlogger_events_total.labels(m.getRemoteHost(), m.getRemoteHost(), ident).inc(elist.size());
            return elist;
        }

        DMetrics.eventlogger_events_ignored_total.labels(m.getRemoteHost(), m.getMethod()).inc();
        LogFactory.getLogger().warn("HttpWebhook: doPost: message is empty; event will be ignored");
        return null;
    }

    private void evaluateRules(DEvent event) {

        if (DProps.yamlConfig == null) return;

        long before = System.nanoTime();

        for (DRulesGroup group : DProps.yamlConfig.getGroups()) {

            if (!group.getEndpoint().equalsIgnoreCase(event.getEndpoint())) continue;

            for (DRule rule : group.getRules()) {

                // check filter
                if (rule.getFilter() != null) {
                    if (rule.getFilter().containsKey("ident")) {
                        if (!rule.getFilter().get("ident").equalsIgnoreCase(event.getIdent())) {
                            continue;
                        }
                    }
                }


                // check expression
                if (rule.getPattern().get("type").equalsIgnoreCase("regex")) {

                    Pattern pattern = Pattern.compile(rule.getPattern().get("expr"), Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(event.getMessage());
                    boolean matchFound = matcher.find();
                    if (matchFound) {
//					System.out.println("regex Match found");
                    } else {
//					System.out.println("regex Match not found");
                        continue;
                    }


                } else if (rule.getPattern().get("type").equalsIgnoreCase("grok")) {

                    GrokCompiler grokCompiler = GrokCompiler.newInstance();
                    grokCompiler.registerDefaultPatterns();

                    final Grok grok = grokCompiler.compile(rule.getPattern().get("expr"));
                    Match gm = grok.match(event.getMessage());
//				System.out.println("GROK PATTERN: " + grok.getNamedRegex());
                    final Map<String, Object> capture = gm.capture();
                    if (capture.size() == 0) {
//					System.out.println("nothing found");
                        continue;
                    }
                    for (String s : capture.keySet()) {
//					System.out.println("GROK RESULT: " + capture.get(s).toString());
                    }

                } else {

                }

                // execute an action
                if (rule.getAction().get("type").equalsIgnoreCase("alarm")) {

                    DAlarm a = new DAlarm(event.getHost(), rule.getName(),
                            DAlarmSeverity.MAJOR, event.getIdent(), "addInfo");
                    AlarmMananger.raiseAlarm(a);

                } else if (rule.getAction().get("type").equalsIgnoreCase("count")) {

                    Counter counter;
                    if (DMetrics.customCounterMetrics.containsKey(rule.getAction().get("metricName"))) {
                        counter = DMetrics.customCounterMetrics.get(rule.getAction().get("metricName"));
                    } else {
                        counter = Counter.build()
                                .name(rule.getAction().get("metricName"))
                                .help(rule.getName())
                                .labelNames("host", "ident")
                                .register();
                    }

                    counter.labels(event.getHost(), event.getIdent()).inc();
                    DMetrics.customCounterMetrics.put(rule.getAction().get("metricName"), counter);

                } else {
                    LogFactory.getLogger().info("no action");
                }

            }

        }

        double duration = (System.nanoTime() - before) * 1.0 / 1000000000;
        DMetrics.eventlogger_rule_evaluation_seconds.labels(event.getEndpoint()).observe(duration);

    }
    
}
