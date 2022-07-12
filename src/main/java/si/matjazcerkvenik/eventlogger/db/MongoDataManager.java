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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.webhooks.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.ArrayList;
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

        logger.info("MongoDataManager: " + getClientName() + " initialized");
    }

    @Override
    public String getClientName() {
        return "Mongo[" + clientId + "]";
    }

    @Override
    public void addWebhookMessage(WebhookMessage webhookMessage) {

        logger.info("MongoDataManager: " + getClientName() + " addWebhookMessage");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("webhook");

//            Document doc = Document.parse(new Gson().toJson(message));
            Document doc = new Document("_id", new ObjectId());
            doc.append("id", webhookMessage.getId())
                    .append("runtimeId", webhookMessage.getRuntimeId())
                    .append("timestamp", webhookMessage.getTimestamp())
                    .append("contentLength", webhookMessage.getContentLength())
                    .append("contentType", webhookMessage.getContentType())
                    .append("method", webhookMessage.getMethod())
                    .append("protocol", webhookMessage.getProtocol())
                    .append("remoteHost", webhookMessage.getRemoteHost())
                    .append("remotePort", webhookMessage.getRemotePort())
                    .append("requestUri", webhookMessage.getRequestUri())
                    .append("headerMap", webhookMessage.getHeaderMap())
                    .append("headerMapString", webhookMessage.getHeaderMapString())
                    .append("parameterMap", webhookMessage.getParameterMap())
                    .append("parameterMapString", webhookMessage.getParameterMapString())
                    .append("body", webhookMessage.getBody());

            // insert one doc
            collection.insertOne(doc);

        } catch (Exception e) {
            logger.error("MongoDataManager: " + getClientName() + " addWebhookMessage: Exception: " + e.getMessage());
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, "webhook", "insert").observe(duration);
        }

    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {

        logger.info("MongoDataManager: " + getClientName() + " getWebhookMessages");

        long before = System.currentTimeMillis();
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("webhook");

            List<Document> docsResultList = collection.find(Filters.eq("runtimeId", DProps.RUNTIME_ID))
                    .sort(Sorts.descending("id"))
                    .limit(100)
                    .into(new ArrayList<>());

            logger.info("MongoDataManager: " + getClientName() + " docsResultList size=" + docsResultList.size());

            List<WebhookMessage> webhookMessageList = new ArrayList<>();

//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();

            for (Document doc : docsResultList) {
                // document: {"_id": {"$oid": "62044887878b4423baf8d9c7"}, "id": 46, "timestamp": {"$numberLong": "1644447879359"}, "contentLength": 1952, "contentType": "application/json", "method": "POST", "protocol": "HTTP/1.1", "remoteHost": "192.168.0.123", "remotePort": 36312, "requestUri": "/alertmonitor/webhook", "body": "{\"receiver\":\"alertmonitor\",\"status\":\"firing\",\"alerts\":[{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"instance\":\"gitlab.iskratel.si:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on gitlab.iskratel.si:22 is down\"},\"startsAt\":\"2022-02-09T18:54:10.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"6a7e625056f7fa79\"},{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"instance\":\"prom.devops.iskratel.cloud:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on prom.devops.iskratel.cloud:22 is down\"},\"startsAt\":\"2022-02-09T18:54:25.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"22e2e031457e143b\"}],\"groupLabels\":{\"alertname\":\"SSH Not Responding\"},\"commonLabels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"commonAnnotations\":{},\"externalURL\":\"http://promvm.home.net:9093\",\"version\":\"4\",\"groupKey\":\"{}/{severity=~\\\"^(critical|major|minor|warning|informational|indeterminate)$\\\"}:{alertname=\\\"SSH Not Responding\\\"}\",\"truncatedAlerts\":0}", "parameterMap": {}, "headerMap": {"content-length": "1952", "host": "192.168.0.16:8080", "content-type": "application/json", "user-agent": "Alertmanager/0.23.0"}}
//                System.out.println("document: " + doc.toJson());
//                WebhookMessage am = gson.fromJson(doc.toJson(), WebhookMessage.class);
//                System.out.println("converted back: " + am.toString());
                WebhookMessage m = new WebhookMessage();
                m.setId(((Number) doc.get("id")).longValue());
                m.setRuntimeId(doc.getString("runtimeId"));
                m.setTimestamp(((Number) doc.get("timestamp")).longValue());
                m.setContentLength(doc.getInteger("contentLength"));
                m.setContentType(doc.getString("contentType"));
                m.setMethod(doc.getString("method"));
                m.setProtocol(doc.getString("protocol"));
                m.setRemoteHost(doc.getString("remoteHost"));
                m.setRemotePort(doc.getInteger("remotePort"));
                m.setRequestUri(doc.getString("requestUri"));
                m.setBody(doc.getString("body"));
                m.setHeaderMapString(doc.getString("headerMapString"));
                m.setParameterMapString(doc.getString("parameterMapString"));

                // there are exceptions thrown if document.getString(xx) does not exist

                webhookMessageList.add(m);
            }

            return webhookMessageList;

        } catch (Exception e) {
            logger.error("MongoDataManager: " + getClientName() + " getWebhookMessages: Exception: ", e);
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, "events", "query").observe(duration);
        }

        return null;
    }

    @Override
    public void addEventMessage(List<DEvent> eventList) {

        if (eventList.isEmpty()) return;

        logger.info("MongoDataManager: " + getClientName() + " addEventMessage: size=" + eventList.size());

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("events");

            List<Document> list = new ArrayList<>();

            for (DEvent e : eventList) {
                Document doc = Document.parse(new Gson().toJson(e));
                list.add(doc);
            }

            collection.insertMany(list, new InsertManyOptions().ordered(false));

        } catch (Exception e) {
            logger.error("MongoDataManager: " + getClientName() + " addEventMessage: Exception: " + e.getMessage());
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, "webhook", "insert").observe(duration);
        }
    }

    @Override
    public List<DEvent> getEventMessages() {

        logger.info("MongoDataManager: " + getClientName() + " getJournal");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection("events");

            List<Document> docsResultList = collection.find()
                    //.sort(Sorts.descending("timestamp"))
                    .limit(1000)
                    .into(new ArrayList<>());

            logger.info("MongoDataManager: " + getClientName() + " docsResultList size=" + docsResultList.size());

            List<DEvent> eventList = new ArrayList<>();

            for (Document doc : docsResultList) {
                DEvent event = new DEvent();
                event.setHost(doc.getString("host"));
                event.setIdent(doc.getString("ident"));
                event.setPid(doc.getString("pid"));
                event.setMessage(doc.getString("message"));
                eventList.add(event);
            }

            return eventList;

        } catch (Exception e) {
            logger.error("MongoDataManager: " + getClientName() + " getJournal: Exception: " + e.getMessage());
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, "events", "query").observe(duration);
        }
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
