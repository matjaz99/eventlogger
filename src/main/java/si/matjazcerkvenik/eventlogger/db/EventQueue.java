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

    public DRequest getNextRequest() {
        if (incReqQueue.isEmpty()) return null;
        return incReqQueue.poll();
    }

}
