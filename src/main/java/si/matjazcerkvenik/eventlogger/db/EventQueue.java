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
package si.matjazcerkvenik.eventlogger.db;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import io.prometheus.client.Counter;
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.eventlogger.model.DAlarmSeverity;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.model.config.DRule;
import si.matjazcerkvenik.eventlogger.model.config.DRulesGroup;
import si.matjazcerkvenik.eventlogger.parsers.*;
import si.matjazcerkvenik.eventlogger.util.AlarmMananger;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventQueue {

    private ConcurrentLinkedQueue<DRequest> incReqQueue = new ConcurrentLinkedQueue<>();

    /**
     * A list containing data waiting to be inserted into DB.
     */
    private ConcurrentLinkedQueue<DEvent> queue = new ConcurrentLinkedQueue<>();

    /**
     * Singleton instance of EventQueue
     */
    private static EventQueue eventQueueInstance = new EventQueue();

    public static EventQueue getInstance() {
        return eventQueueInstance;
    }

    public void addIncomingRequest(DRequest dRequest) {
        incReqQueue.add(dRequest);
    }

    /**
     * Add events to queue, waiting to be eventually stored in DB.
     * @param eventList
     */
    public void addEvents(List<DEvent> eventList) {
        queue.addAll(eventList);
    }

    public long getQueueSize() {
        return queue.stream().count();
    }

    public int getIncReqQueueSize() {
        return incReqQueue.size();
    }

    public List<DEvent> getNextBatch() {
        List<DEvent> list = new ArrayList<>();
        for (int i = 0; i < DProps.EVENTLOGGER_MONGODB_BULK_INSERT_MAX_SIZE; i++) {
            list.add(queue.poll());
            if (queue.isEmpty()) break;
        }
        return list;
    }

    public void processRequestsQueue() {

        while (!incReqQueue.isEmpty()) {
            DRequest dRequest = incReqQueue.poll();
            List<DEvent> eventList = null;
            IEventParser parser = null;

            try {

                if (dRequest.getRequestUri().equalsIgnoreCase("/eventlogger/webhook/fluentd-syslog")) {

                    parser = new FluentSyslogParser();

                } else if (dRequest.getRequestUri().equalsIgnoreCase("/eventlogger/webhook/fluentd-tail")) {

                    parser = new FluentTailParser();

                } else if (dRequest.getRequestUri().equalsIgnoreCase("/eventlogger/webhook/http")) {

                    if (dRequest.getMethod().equalsIgnoreCase("GET")) {
                        parser = new HttpGetParser();
                    } else if (dRequest.getMethod().equalsIgnoreCase("POST")) {
                        parser = new HttpPostParser();
                    }

                } else {
                    LogFactory.getLogger().warn("ReceiverServlet: doPost: endpoint not supported: " + dRequest.getRequestUri());
                    parser = new GenericPostParser();
                }

                eventList = parser.parseRequest(dRequest);

            } catch (EventParserException e) {
                LogFactory.getLogger().warn("ReceiverServlet: doPost: fallback to generic parser");
                parser = new GenericPostParser();
                try {
                    eventList = parser.parseRequest(dRequest);
                } catch (EventParserException ex) {
                    LogFactory.getLogger().warn("ReceiverServlet: doPost: EventParserException: no suitable parser");
                }
            }


            if (eventList == null || eventList.isEmpty()) {
                LogFactory.getLogger().warn("ReceiverServlet: doPost: eventList is empty: " + dRequest.toString());
                return;
            }

            for (DEvent e : eventList) {
                evaluateRules(e);
            }
//            for (Iterator<DEvent> it = eventList.iterator(); it.hasNext();) {
//                DEvent e = it.next();
//                e = evaluateRules(e);
//                if (e == null) it.remove();
//            }


            if (DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC == 0) {
                // send immediately to db
                IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
                iDataManager.addEvents(eventList);
                DataManagerFactory.getInstance().returnClient(iDataManager);
            } else {
                // put to event queue
                addEvents(eventList);
            }

        }
    }




    private GrokCompiler grokCompiler = GrokCompiler.newInstance();

    {
        grokCompiler.registerDefaultPatterns();
    }

    /**
     * Compare each event against rule definition.
     */
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
                    if (rule.getFilter().containsKey("host")) {
                        if (!rule.getFilter().get("host").equalsIgnoreCase(event.getHost())) {
                            continue;
                        }
                    }
                    if (rule.getFilter().containsKey("tag")) {
                        if (!rule.getFilter().get("tag").equalsIgnoreCase(event.getTag())) {
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

                    final Grok grok = grokCompiler.compile(rule.getPattern().get("expr"));
                    Match gm = grok.match(event.getMessage());
//				System.out.println("GROK PATTERN: " + grok.getNamedRegex());
                    final Map<String, Object> capture = gm.capture();
                    if (capture.isEmpty()) {
//					System.out.println("nothing found");
                        continue;
                    }
//                    for (String s : capture.keySet()) {
//					System.out.println("GROK RESULT: " + capture.get(s).toString());
//                    }

                } else {

                }


                // execute an action
                if (rule.getAction().get("type").equalsIgnoreCase("alarm")) {

                    int severity = DAlarmSeverity.getSeverity(rule.getAction().get("severity"));
                    DAlarm a = new DAlarm(event.getRemoteAddress(), event.getHost(), rule.getName(),
                            severity, event.getIdent(), event.getTag(),
                            rule.getAction().get("addInfo"), event.getMessage());
                    a.setNotificationType(rule.getAction().get("type"));
                    AlarmMananger.raiseAlarm(a);

                    DMetrics.eventlogger_rule_actions_total.labels(rule.getAction().get("type")).inc();
                    rule.increaseHits();

                } else if (rule.getAction().get("type").equalsIgnoreCase("clear")) {

                    DAlarm a = new DAlarm(event.getRemoteAddress(), event.getHost(), rule.getName(),
                            DAlarmSeverity.CLEAR, event.getIdent(), event.getTag(), null, event.getMessage());
                    a.setNotificationType(rule.getAction().get("type"));
                    AlarmMananger.clearAlarm(a);

                    DMetrics.eventlogger_rule_actions_total.labels(rule.getAction().get("type")).inc();
                    rule.increaseHits();

                } else if (rule.getAction().get("type").equalsIgnoreCase("event")) {

                    DAlarm a = new DAlarm(event.getRemoteAddress(), event.getHost(), rule.getName(),
                            DAlarmSeverity.INFORMATIONAL, event.getIdent(), event.getTag(),
                            rule.getAction().get("addInfo"), event.getMessage());
                    a.setNotificationType(rule.getAction().get("type"));
                    AlarmMananger.sendEvent(a);

                    DMetrics.eventlogger_rule_actions_total.labels(rule.getAction().get("type")).inc();
                    rule.increaseHits();

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

                    DMetrics.eventlogger_rule_actions_total.labels(rule.getAction().get("type")).inc();
                    rule.increaseHits();

                } else {
                    LogFactory.getLogger().info("unsupported action: " + rule.getAction().get("type"));
                }

            } // end for each rule

        }

        double duration = (System.nanoTime() - before) * 1.0 / 1000000000;
        DMetrics.eventlogger_rule_evaluation_seconds.labels(event.getEndpoint()).observe(duration);

    }

}
