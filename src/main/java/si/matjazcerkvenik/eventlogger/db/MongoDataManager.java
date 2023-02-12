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
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import si.matjazcerkvenik.eventlogger.model.*;
import si.matjazcerkvenik.eventlogger.util.AlarmMananger;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MongoDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();
    public static final String dbName = "eventlogger";
    public static final String dbCollectionEvents = "events";
    public static final String dbCollectionRequests = "requests";
    private MongoClient mongoClient;
    private int clientId = 0;
    private String clientName;
    private static boolean dbIndexCreated = false;

    private static DAlarm mongoAlarm = new DAlarm("eventlogger", "MongoDB down",
            DAlarmSeverity.CRITICAL,
            DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING,
            "Cannot connect to MongoDB");

    public MongoDataManager(int id) {

        clientId = id;
        clientName = "Mongo[" + clientId + "]";

        int timeoutSeconds = 5;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(DProps.EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS);
                    builder.readTimeout(DProps.EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC, SECONDS);
                })
                .applyToClusterSettings( builder -> builder.serverSelectionTimeout(DProps.EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS))
                .applyConnectionString(new ConnectionString(DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING))
                .build();

        mongoClient = MongoClients.create(settings);

        initializeDatabase();

    }

    /**
     * Create collection and create indexes
     */
    private void initializeDatabase() {
        if (!dbIndexCreated) {
            try {
                MongoDatabase database = mongoClient.getDatabase(dbName);

                MongoCollection<Document> coll = database.getCollection(dbCollectionEvents);
                if (coll == null) {
                    logger.info(getClientName() + " creating collection: " + dbCollectionEvents);
                    database.createCollection(dbCollectionEvents);
                }
                // create indexes
                logger.info(getClientName() + " creating index: host");
                coll.createIndex(Indexes.ascending("host"));
                logger.info(getClientName() + " creating index: ident");
                coll.createIndex(Indexes.ascending("ident"));
                dbIndexCreated = true;
                logger.info(getClientName() + " database initialized");
                logger.info(getClientName() + " client ready");
                AlarmMananger.clearAlarm(mongoAlarm);
            } catch (Exception e) {
                AlarmMananger.raiseAlarm(mongoAlarm);
                logger.error(getClientName() + " error initializing database: " + e.getMessage());
            }
        }

    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void addHttpRequest(DRequest DRequest) {

        logger.info(getClientName() + " addHttpRequest");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionRequests);

//            Document doc = Document.parse(new Gson().toJson(message));
            Document doc = new Document("_id", new ObjectId());
            doc.append("id", DRequest.getId())
                    .append("runtimeId", DRequest.getRuntimeId())
                    .append("timestamp", DRequest.getTimestamp())
                    .append("contentLength", DRequest.getContentLength())
                    .append("contentType", DRequest.getContentType())
                    .append("method", DRequest.getMethod())
                    .append("protocol", DRequest.getProtocol())
                    .append("remoteHost", DRequest.getRemoteHost())
                    .append("remotePort", DRequest.getRemotePort())
                    .append("requestUri", DRequest.getRequestUri())
                    .append("headerMap", DRequest.getHeaderMap())
                    .append("headerMapString", DRequest.getHeaderMapString())
                    .append("parameterMap", DRequest.getParameterMap())
                    .append("parameterMapString", DRequest.getParameterMapString())
                    .append("body", DRequest.getBody());

            // insert one doc
            collection.insertOne(doc);

            AlarmMananger.clearAlarm(mongoAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error(getClientName() + " addHttpRequest: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionRequests, "insert").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionRequests, "insert").observe(duration);
        }

    }

    @Override
    public List<DRequest> getHttpRequests() {

        logger.info(getClientName() + " getHttpRequests");

        long before = System.currentTimeMillis();
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionRequests);

            List<Document> docsResultList = collection.find(Filters.eq("runtimeId", DProps.RUNTIME_ID))
                    .sort(Sorts.descending("timestamp", "id"))
                    .limit(100)
                    .into(new ArrayList<>());

            logger.info(getClientName() + " docsResultList size=" + docsResultList.size());

            List<DRequest> dRequestList = new ArrayList<>();

//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();

            for (Document doc : docsResultList) {
                // document: {"_id": {"$oid": "62044887878b4423baf8d9c7"}, "id": 46, "timestamp": {"$numberLong": "1644447879359"}, "contentLength": 1952, "contentType": "application/json", "method": "POST", "protocol": "HTTP/1.1", "remoteHost": "192.168.0.123", "remotePort": 36312, "requestUri": "/alertmonitor/webhook", "body": "{\"receiver\":\"alertmonitor\",\"status\":\"firing\",\"alerts\":[{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"instance\":\"gitlab.iskratel.si:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on gitlab.iskratel.si:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on gitlab.iskratel.si:22 is down\"},\"startsAt\":\"2022-02-09T18:54:10.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"6a7e625056f7fa79\"},{\"status\":\"firing\",\"labels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"info\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"instance\":\"prom.devops.iskratel.cloud:22\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"annotations\":{\"description\":\"SSH on prom.devops.iskratel.cloud:22 has been down for more than 10 minutes.\",\"summary\":\"SSH on prom.devops.iskratel.cloud:22 is down\"},\"startsAt\":\"2022-02-09T18:54:25.322Z\",\"endsAt\":\"0001-01-01T00:00:00Z\",\"generatorURL\":\"http://promvm.home.net/prometheus/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-ssh%22%7D+%3D%3D+0\\u0026g0.tab=1\",\"fingerprint\":\"22e2e031457e143b\"}],\"groupLabels\":{\"alertname\":\"SSH Not Responding\"},\"commonLabels\":{\"alarmcode\":\"300070\",\"alertname\":\"SSH Not Responding\",\"cluster\":\"monis-cluster\",\"job\":\"blackbox-ssh\",\"monitor\":\"monis\",\"region\":\"si-home\",\"severity\":\"minor\",\"tags\":\"ssh\"},\"commonAnnotations\":{},\"externalURL\":\"http://promvm.home.net:9093\",\"version\":\"4\",\"groupKey\":\"{}/{severity=~\\\"^(critical|major|minor|warning|informational|indeterminate)$\\\"}:{alertname=\\\"SSH Not Responding\\\"}\",\"truncatedAlerts\":0}", "parameterMap": {}, "headerMap": {"content-length": "1952", "host": "192.168.0.16:8080", "content-type": "application/json", "user-agent": "Alertmanager/0.23.0"}}
//                System.out.println("document: " + doc.toJson());
//                WebhookMessage am = gson.fromJson(doc.toJson(), WebhookMessage.class);
//                System.out.println("converted back: " + am.toString());
                DRequest m = new DRequest();
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

                dRequestList.add(m);
            }

            AlarmMananger.clearAlarm(mongoAlarm);

            return dRequestList;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error(getClientName() + " getHttpRequests: Exception: ", e);
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionRequests, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionRequests, "query").observe(duration);
        }

        return null;
    }

    @Override
    public void addEvents(List<DEvent> eventList) {

        if (eventList == null || eventList.isEmpty()) return;

        logger.info(getClientName() + " addEvents: size=" + eventList.size());

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);

            List<Document> list = new ArrayList<>();

            for (DEvent e : eventList) {
                Document doc = Document.parse(new Gson().toJson(e));
                list.add(doc);
                tryGrokFilter(e.getMessage());
            }

            collection.insertMany(list, new InsertManyOptions().ordered(true));

            AlarmMananger.clearAlarm(mongoAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error(getClientName() + " addEvents: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "insert").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "insert").observe(duration);
        }
    }

    @Override
    public List<DEvent> getEvents(DFilter filter) {

        logger.info(getClientName() + " getEvents: filter=" + filter);

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);

            List<Document> docsResultList = null;

            if (filter == null) {
                docsResultList = collection.find()
                        .sort(Sorts.descending("timestamp", "id"))
                        .limit(1000)
                        .into(new ArrayList<>());
            } else {
                Bson bsonFilter = prepareBsonFilter(filter);
                Bson bsonSorting;
                if (filter.isAscending()) {
                    bsonSorting = Sorts.ascending("timestamp", "id");
                } else {
                    bsonSorting = Sorts.descending("timestamp", "id");
                }
                docsResultList = collection.find(bsonFilter)
                        .sort(bsonSorting)
                        .limit(filter.getLimit())
                        .into(new ArrayList<>());
            }

            logger.info(getClientName() + " docsResultList size=" + docsResultList.size());

            List<DEvent> eventList = new ArrayList<>();

            for (Document doc : docsResultList) {
                logger.trace(getClientName() + " doc=" + doc.toJson());
                DEvent event = new DEvent();
                event.setId(((Number) doc.get("id")).longValue());
                event.setRuntimeId(doc.getString("runtimeId"));
                event.setTimestamp(((Number) doc.get("timestamp", 0)).longValue());
                event.setHost(doc.getString("host"));
                event.setIdent(doc.getString("ident"));
                event.setPid(doc.getString("pid"));
                event.setTag(doc.getString("tag"));
                event.setMessage(doc.getString("message"));
                event.setEventSource(doc.get("eventSource", "null"));
                eventList.add(event);
            }

            AlarmMananger.clearAlarm(mongoAlarm);

            return eventList;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error(getClientName() + " getEvents: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "query").observe(duration);
        }
        return null;
    }

    public Bson prepareBsonFilter(DFilter filter) {
        Bson bsonFilter = null;
        List<Bson> tempList = new ArrayList<>();
        if (filter.getHosts() != null && filter.getHosts().length > 0) {
            Bson[] bArray = new Bson[filter.getHosts().length];
            for (int i = 0; i < filter.getHosts().length; i++) {
                Bson b = Filters.eq("host", filter.getHosts()[i]);
                bArray[i] = b;
            }
            Bson hostsFilter = Filters.or(bArray);
            tempList.add(hostsFilter);
        }
        if (filter.getIdents() != null && filter.getIdents().length > 0) {
            Bson[] bArray = new Bson[filter.getIdents().length];
            for (int i = 0; i < filter.getIdents().length; i++) {
                Bson b = Filters.eq("ident", filter.getIdents()[i]);
                bArray[i] = b;
            }
            Bson identsFilter = Filters.or(bArray);
            tempList.add(identsFilter);
        }
        if (filter.getSearchPattern() != null && filter.getSearchPattern().length() > 0) {
            Bson b = Filters.regex("message", filter.getSearchPattern());
            tempList.add(b);
            // https://www.mongodb.com/docs/manual/reference/operator/query/regex/
            //https://www.mongodb.com/docs/manual/reference/operator/query/regex/#std-label-syntax-restrictions
        }

        if (tempList.isEmpty()) {
            return null;
        }
        Bson[] barr = new Bson[tempList.size()];
        tempList.toArray(barr);

        bsonFilter = Filters.and(barr);
        logger.debug("prepareBsonFilter: " + bsonFilter.toString());
        return bsonFilter;
    }

    @Override
    public List<String> getDistinctKeys(String key) {
        logger.info(getClientName() + " getDistinctKeys");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);

            DistinctIterable<String> docs = collection.distinct(key, String.class);

            List<String> resultList = new ArrayList<>();

            MongoCursor<String> results = docs.iterator();
            while(results.hasNext()) {
                resultList.add(results.next());
            }

            logger.info(getClientName() + " getDistinctKeys: for=" + key + ", size=" + resultList.size());

            AlarmMananger.clearAlarm(mongoAlarm);

            return resultList;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error(getClientName() + " getDistinctKeys: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "query").observe(duration);
        }
        return null;
    }

    @Override
    public void cleanDB() {

        logger.info(getClientName() + " cleanDB: started");
        long before = System.currentTimeMillis();

        try {

            long daysInMillis = Integer.toUnsignedLong(DProps.EVENTLOGGER_DATA_RETENTION_DAYS) * 24 * 3600 * 1000;
            long diff = (System.currentTimeMillis() - daysInMillis);
            Bson filter = Filters.lte("timestamp", diff);

            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionRequests);
            DeleteResult resultDeleteMany = collection.deleteMany(filter);
            logger.info( getClientName() + " cleanDB [requests]: size=" + resultDeleteMany.getDeletedCount());

            MongoCollection<Document> collection2 = db.getCollection(dbCollectionEvents);
            DeleteResult resultDeleteMany2 = collection2.deleteMany(filter);
            logger.info( getClientName() + " cleanDB [events]: size=" + resultDeleteMany2.getDeletedCount());

            AlarmMananger.clearAlarm(mongoAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoAlarm);
            logger.error( getClientName() + " cleanDB: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, "/", "delete").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, "/", "delete").observe(duration);
        }

    }

    @Override
    public void close() {
        mongoClient.close();
    }





    /* Create a new grokCompiler instance */
    GrokCompiler grokCompiler = GrokCompiler.newInstance();
    { grokCompiler.registerDefaultPatterns(); }

    private void tryGrokFilter(String line) {

        Grok grok = grokCompiler.compile("%{GREEDYDATA}(error|Error|ERROR)%{GREEDYDATA}");
        Match gm = grok.match(line);
        Map<String, Object> capture = gm.capture();
        if (!capture.isEmpty()) logger.trace("GROK RESULT[" + capture.size() + "]: " + capture.toString());
    }

}
