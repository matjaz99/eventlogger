package si.matjazcerkvenik.eventlogger.db;

import java.util.TimerTask;

public class DbMaintenanceTask extends TimerTask {

    @Override
    public void run() {

        IDataManager iDataManager = DataManagerFactory.getDataManager();
        iDataManager.cleanDB();

    }
}
