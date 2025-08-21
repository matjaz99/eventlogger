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

import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.eventlogger.util.DProps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {

    /**
     * A list containing requests waiting to be processed.
     */
    private ConcurrentLinkedQueue<DRequest> incReqQueue = new ConcurrentLinkedQueue<>();

    /**
     * A list containing events waiting to be inserted into DB.
     */
    private ConcurrentLinkedQueue<DEvent> dbQueue = new ConcurrentLinkedQueue<>();

    /**
     * Singleton instance of EventQueue
     */
    private static EventQueue eventQueueInstance = new EventQueue();

    public static EventQueue getInstance() {
        return eventQueueInstance;
    }

    /**
     * Add incoming request to the queue. It is waiting to be processed in separate process (see Processor Thread)
     * @param dRequest
     */
    public void addIncomingRequest(DRequest dRequest) {
        incReqQueue.add(dRequest);
    }

    /**
     * Add processed events to queue, waiting to be eventually stored in DB. A separate thread will insert events
     * into the DB.
     * @param eventList
     */
    public void addEvents(List<DEvent> eventList) {
        dbQueue.addAll(eventList);
    }

    public long getDbQueueSize() {
        return dbQueue.stream().count();
    }

    public int getIncReqQueueSize() {
        return incReqQueue.size();
    }

    /**
     * Return next batch of events from the queue to be inserted into DB.
     * The size of batch is limited to configured max bulk size.
     * @return batch (list) of events
     */
    public List<DEvent> getNextBatch() {
        List<DEvent> list = new ArrayList<>();
        for (int i = 0; i < DProps.EVENTLOGGER_MONGODB_BULK_INSERT_MAX_SIZE; i++) {
            list.add(dbQueue.poll());
            if (dbQueue.isEmpty()) break;
        }
        return list;
    }

    /**
     * Get next request from the queue.
     * @return request
     */
    public DRequest getNextRequest() {
        if (incReqQueue.isEmpty()) return null;
        return incReqQueue.poll();
    }

}
