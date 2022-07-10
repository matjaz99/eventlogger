package si.matjazcerkvenik.eventlogger.db;

import si.matjazcerkvenik.eventlogger.model.DMessage;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.LinkedList;
import java.util.List;

public class MemoryDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();

    public static List<WebhookMessage> webhookMessages = new LinkedList<>();
    public static List<DMessage> eventMessages = new LinkedList<>();

    public MemoryDataManager() {
        logger.info("MemoryDataManager initialized");
    }

    @Override
    public void addWebhookMessage(WebhookMessage webhookMessage) {
        webhookMessages.add(webhookMessage);
        if (webhookMessages.size() > 100) {
            webhookMessages.remove(0);
        }
    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    @Override
    public void addEventMessage(DMessage message) {
        eventMessages.add(message);
        if (eventMessages.size() > 1000) {
            eventMessages.remove(0);
        }
    }

    @Override
    public List<DMessage> getEventMessages() {
        return eventMessages;
    }

    @Override
    public void cleanDB() {
        // not applicable
    }

    @Override
    public void close() {
        // not applicable
    }
}
