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
package si.matjazcerkvenik.eventlogger.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import si.matjazcerkvenik.eventlogger.model.DMessage;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.webhooks.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();
    public static String dbName = "eventlogger";
    private MongoClient mongoClient;
    private int clientId = 0;

    public MongoDataManager(int id) {

        clientId = id;

        int timeoutSeconds = 5;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(timeoutSeconds * 1000, MILLISECONDS);
                    builder.readTimeout(timeoutSeconds * 1000, MILLISECONDS);
                })
                .applyToClusterSettings( builder -> builder.serverSelectionTimeout(timeoutSeconds * 1000, MILLISECONDS))
                .applyConnectionString(new ConnectionString(DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING))
                .build();

        mongoClient = MongoClients.create(settings);

        logger.info("MongoDataManager: Mongo[" + clientId + "] initialized");
    }

    @Override
    public String getClientName() {
        return "Mongo[" + clientId + "]";
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
