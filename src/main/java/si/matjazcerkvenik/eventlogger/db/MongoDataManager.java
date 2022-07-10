package si.matjazcerkvenik.eventlogger.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import si.matjazcerkvenik.eventlogger.model.DMessage;
import si.matjazcerkvenik.eventlogger.util.DelProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.web.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();
    public static String dbName = "eventlogger";
    private MongoClient mongoClient;

    public MongoDataManager() {

        int timeoutSeconds = 5;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(timeoutSeconds * 1000, MILLISECONDS);
                    builder.readTimeout(timeoutSeconds * 1000, MILLISECONDS);
                })
                .applyToClusterSettings( builder -> builder.serverSelectionTimeout(timeoutSeconds * 1000, MILLISECONDS))
                .applyConnectionString(new ConnectionString(DelProps.EVENTLOGGER_MONGODB_CONNECTION_STRING))
                .build();

        mongoClient = MongoClients.create(settings);

        logger.info("MongoDataManager initialized");
    }

    @Override
    public void addWebhookMessage(WebhookMessage webhookMessage) {

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return null;
    }

    @Override
    public void addEventMessage(DMessage message) {

    }

    @Override
    public List<DMessage> getEventMessages() {
        return null;
    }

    @Override
    public void cleanDB() {

    }

    @Override
    public void close() {
        mongoClient.close();
    }
}
