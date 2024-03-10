package si.matjazcerkvenik.eventlogger.db;


import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.List;

public class FlushQueueThread extends Thread {

    public FlushQueueThread(String name) {
        super(name);
    }

    @Override
    public void run() {

        LogFactory.getLogger().info("Start FlushQueue Thread");

        while (true) {

            try {
                Thread.sleep(DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            List<DEvent> eventList = EventQueue.getInstance().getAllEvents();

            LogFactory.getLogger().info("FlushQueueThread: queue size=" + eventList.size());
            if (eventList.isEmpty()) continue;

            // TODO check and limit the size of batch
            if (eventList.size() > DProps.EVENTLOGGER_MONGODB_BATCH_INSERT_MAX_SIZE) {

            }

            IDataManager iDataManager = DataManagerFactory.getInstance().getClient();
            iDataManager.addEvents(eventList);
            DataManagerFactory.getInstance().returnClient(iDataManager);

        }



    }

}
