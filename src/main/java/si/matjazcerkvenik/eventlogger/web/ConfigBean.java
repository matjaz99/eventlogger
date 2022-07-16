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

}
