package si.matjazcerkvenik.eventlogger.db;


import si.matjazcerkvenik.eventlogger.util.DelProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

public class DataManagerFactory {

    public static IDataManager getDataManager() {

        IDataManager iDataManager = null;

        if (DelProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("memory")) {
            iDataManager = new MemoryDataManager();
        } else if (DelProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("mongodb")) {
            iDataManager = new MongoDataManager();
        } else {
            LogFactory.getLogger().warn("DataManagerFactory:getDataManager: unknown storage type: " + DelProps.EVENTLOGGER_STORAGE_TYPE);
        }

        return iDataManager;

    }

}
