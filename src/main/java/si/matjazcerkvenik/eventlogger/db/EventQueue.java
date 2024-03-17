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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {

    private ConcurrentLinkedQueue<DEvent> queue = new ConcurrentLinkedQueue<>();



    private static EventQueue eventQueue = new EventQueue();

    public static EventQueue getInstance() {
        return eventQueue;
    }

    public void addEvents(List<DEvent> eventList) {
        queue.addAll(eventList);
    }

    public List<DEvent> getAllEvents() {
        List<DEvent> list = new ArrayList<>();
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            list.add(queue.poll());
        }
        return list;
    }

}
