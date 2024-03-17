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
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.util.ArrayList;
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
            IDataManager iDataManager = DataManagerFactory.getInstance().getClient();

            List<DEvent> tempList = new ArrayList<>();
            int i = 0;
            for (DEvent e : eventList) {
                tempList.add(e);
                i++;
                if (i == DProps.EVENTLOGGER_MONGODB_BATCH_INSERT_MAX_SIZE) {
                    iDataManager.addEvents(tempList);
                    i = 0;
                    tempList.clear();
                }
            }
            if (!tempList.isEmpty()) iDataManager.addEvents(tempList);

            DataManagerFactory.getInstance().returnClient(iDataManager);

        }



    }

}
