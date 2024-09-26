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
import si.matjazcerkvenik.eventlogger.util.DProps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {

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

    public List<DEvent> getNextBatch() {
        List<DEvent> list = new ArrayList<>();
        for (int i = 0; i < DProps.EVENTLOGGER_MONGODB_BULK_INSERT_MAX_SIZE; i++) {
            list.add(queue.poll());
            if (queue.isEmpty()) break;
        }
        return list;
    }

}
