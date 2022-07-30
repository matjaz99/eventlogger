package si.matjazcerkvenik.eventlogger.web;

import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class ConfigBean {

    public String getDataRetention() {
        return Integer.toString(DProps.EVENTLOGGER_DATA_RETENTION_DAYS);
    }

    public void setDataRetention(String s) {
        try {
            DProps.EVENTLOGGER_DATA_RETENTION_DAYS = Integer.parseInt(s.trim());
            LogFactory.getLogger().info("ConfigBean: set EVENTLOGGER_DATA_RETENTION_DAYS: " + DProps.EVENTLOGGER_DATA_RETENTION_DAYS);
        } catch (NumberFormatException e) {
            LogFactory.getLogger().error("ConfigBean: set EVENTLOGGER_DATA_RETENTION_DAYS failed: " + e.getMessage());
        }
    }

    public long getEventsCount() {
        return DProps.webhookEventsReceivedCount;
    }

    public String getRuntimeId() {
        return DProps.RUNTIME_ID;
    }

    public long getStartUpTimestamp() {
        return DProps.START_UP_TIMESTAMP;
    }

    public String getVersion() {
        return DProps.VERSION;
    }

    public boolean isContainerized() {
        return DProps.IS_CONTAINERIZED;
    }

    public String getLocalIp() {
        return DProps.LOCAL_IP;
    }

    public String getStorageType() {
        return DProps.EVENTLOGGER_STORAGE_TYPE;
    }

    public String getMongoConnectionString() {
        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("mongodb"))
            return DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING;
        return "-";
    }

    public int getDbPoolSize() {
        return DProps.EVENTLOGGER_DB_POOL_SIZE;
    }

}
