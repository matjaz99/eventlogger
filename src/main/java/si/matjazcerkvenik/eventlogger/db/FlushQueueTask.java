package si.matjazcerkvenik.eventlogger.db;


import si.matjazcerkvenik.eventlogger.model.DEvent;

import java.util.List;
import java.util.TimerTask;

public class FlushQueueTask extends TimerTask {

    @Override
    public void run() {

        List<DEvent> eventList = EventQueue.getInstance().getAllEvents();

        System.out.println("FlushQueueTask: size=" + eventList.size());

        // TODO check and limit the size of batch

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        iDataManager.addEvents(eventList);
        DataManagerFactory.getInstance().returnClient(iDataManager);

    }

}
