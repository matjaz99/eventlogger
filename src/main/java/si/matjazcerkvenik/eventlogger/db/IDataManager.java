package si.matjazcerkvenik.eventlogger.db;


import si.matjazcerkvenik.eventlogger.model.DMessage;
import si.matjazcerkvenik.eventlogger.web.WebhookMessage;

import java.util.List;

public interface IDataManager {

    public void addWebhookMessage(WebhookMessage webhookMessage);

    public List<WebhookMessage> getWebhookMessages();

    public void addEventMessage(DMessage message);

    public List<DMessage> getEventMessages();

    public void cleanDB();

    public void close();

}
