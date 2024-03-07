package si.matjazcerkvenik.eventlogger.db;

import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.DProps;

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
