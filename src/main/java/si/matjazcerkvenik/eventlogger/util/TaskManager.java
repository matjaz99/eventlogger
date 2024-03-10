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
package si.matjazcerkvenik.eventlogger.util;

import si.matjazcerkvenik.eventlogger.db.DbMaintenanceTask;
import si.matjazcerkvenik.eventlogger.db.FlushQueueThread;

import java.util.Timer;

public class TaskManager {

    private static TaskManager taskManager;

    private Timer dbMaintenanceTimer = null;
    private DbMaintenanceTask dbMaintenanceTask = null;

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (taskManager == null) taskManager = new TaskManager();
        return taskManager;
    }

    public void startDbMaintenanceTimer() {

        if (dbMaintenanceTask == null) {
            LogFactory.getLogger().info("Start DB Maintenance Task");
            dbMaintenanceTimer = new Timer("DbMaintenanceTimer");
            dbMaintenanceTask = new DbMaintenanceTask();
            dbMaintenanceTimer.schedule(dbMaintenanceTask, 23 * 1000, 1 * 3600 * 1000);
        }

    }

    public void stopDbMaintenanceTimer() {
        if (dbMaintenanceTimer != null) {
            dbMaintenanceTimer.cancel();
            dbMaintenanceTimer = null;
        }
        if (dbMaintenanceTask != null) {
            dbMaintenanceTask.cancel();
            dbMaintenanceTask = null;
        }
    }


//    private Timer flushQueueTimer = null;
//    private FlushQueueTask flushQueueTask = null;
    private FlushQueueThread flushQueueThread;

    public void startFlushQueueTimer() {

        if (DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC == 0) return;

//        if (flushQueueTask == null) {
//            LogFactory.getLogger().info("Start FlushQueue Task");
//            flushQueueTimer = new Timer("FlushQueueTimer");
//            flushQueueTask = new FlushQueueTask();
//            flushQueueTimer.schedule(flushQueueTask, 0, DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC * 1000);
//        }

        flushQueueThread = new FlushQueueThread("FlushQueueThread");
        flushQueueThread.start();

    }

    public void stopFlushQueueTimer() {
//        if (flushQueueTimer != null) {
//            flushQueueTimer.cancel();
//            flushQueueTimer = null;
//        }
//        if (flushQueueTask != null) {
//            flushQueueTask.cancel();
//            flushQueueTask = null;
//        }
        flushQueueThread.interrupt();
        flushQueueThread = null;
    }


}
