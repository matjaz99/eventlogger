package si.matjazcerkvenik.eventlogger.db;


import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.List;
import java.util.TimerTask;

public class FlushQueueTask extends TimerTask {

    @Override
    public void run() {

        List<DEvent> eventList = EventQueue.getInstance().getAllEvents();

        LogFactory.getLogger().info("FlushQueueTask: queue size=" + eventList.size());
        if (eventList.isEmpty()) return;

        // TODO check and limit the size of batch

        IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
        iDataManager.addEvents(eventList);
        DataManagerFactory.getInstance().returnClient(iDataManager);

    }

}
