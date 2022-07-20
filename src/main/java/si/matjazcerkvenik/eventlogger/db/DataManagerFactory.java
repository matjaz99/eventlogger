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


import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.util.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class DataManagerFactory {

    private int count = 0;
    private List<IDataManager> pool = new ArrayList<>();

    private static DataManagerFactory instance;

    private DataManagerFactory() {
        for (int i = 0; i < DProps.EVENTLOGGER_DB_POOL_SIZE; i++) {
            pool.add(createNewClient());
        }
    }

    public static DataManagerFactory getInstance() {
        if (instance == null) {
            instance = new DataManagerFactory();
            TaskManager.getInstance().startDbMaintenanceTimer();
        }
        return instance;
    }

    private IDataManager createNewClient() {
        IDataManager iDataManager = null;

        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) {
            iDataManager = new MemoryDataManager(count++);
        } else if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("mongodb")) {
            iDataManager = new MongoDataManager(count++);
        } else {
            LogFactory.getLogger().warn("DataManagerFactory:getDataManager: unknown storage type: " + DProps.EVENTLOGGER_STORAGE_TYPE);
        }

        return iDataManager;

    }

    public synchronized IDataManager getClient() {

        while (pool.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                LogFactory.getLogger().error("getClient: Error " + ex.getMessage());
            }
        }
        return pool.remove(0);

    }

    public synchronized void returnClient(IDataManager iDataManager) {
        pool.add(iDataManager);
        notifyAll();
    }

    /**
     * Close and remove all clients
     */
    public void destroy() {
        while (!pool.isEmpty()) {
            pool.remove(0).close();
        }
    }

}
