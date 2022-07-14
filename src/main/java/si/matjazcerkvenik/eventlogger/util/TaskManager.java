package si.matjazcerkvenik.eventlogger.util;

import si.matjazcerkvenik.eventlogger.db.DbMaintenanceTask;

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
}
