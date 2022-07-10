package si.matjazcerkvenik.eventlogger.web;


import si.matjazcerkvenik.eventlogger.db.DataManagerFactory;
import si.matjazcerkvenik.eventlogger.db.IDataManager;
import si.matjazcerkvenik.eventlogger.model.DMessage;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.List;

@ManagedBean
@SessionScoped
public class BackendBean {

    public List<WebhookMessage> getWebhookMessages() {
        IDataManager iDataManager = DataManagerFactory.getDataManager();
        return iDataManager.getWebhookMessages();
    }

    public List<DMessage> getEventMessages() {
        IDataManager iDataManager = DataManagerFactory.getDataManager();
        return iDataManager.getEventMessages();
    }

}
