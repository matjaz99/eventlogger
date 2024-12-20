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
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import si.matjazcerkvenik.eventlogger.model.*;
import si.matjazcerkvenik.eventlogger.util.AlarmMananger;
import si.matjazcerkvenik.eventlogger.util.DMetrics;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDataManager implements IDataManager {

    private static final SimpleLogger logger = LogFactory.getLogger();
    public static String dbName = "eventlogger";
    public static final String dbCollectionEvents = "events";
    private MongoClient mongoClient;
    private int clientId = 0;
    private String clientName;
    /** A flag indicating that index was already created (at startup of eventlogger). */
    private static boolean dbIndexCreated = false;

    private static DAlarm mongoDownAlarm = new DAlarm("localhost", "localhost",
            "MongoDB down", DAlarmSeverity.CRITICAL, "mongodb", "mongodb", null,
            "Cannot connect to MongoDB on " + DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING);

    public MongoDataManager(int id) {

        clientId = id;
        clientName = "Mongo[" + clientId + "]";

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(DProps.EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS);
                    builder.readTimeout(DProps.EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC, SECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(DProps.EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC, SECONDS))
                .applyConnectionString(new ConnectionString(DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING))
                .codecRegistry(codecRegistry)
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
                // TODO CHECK if index exists
                // TODO VERIFY if objects are not in DB anymore, are they also deleted from index?
                // create indexes
                logger.info(getClientName() + " creating index: timestamp");
                coll.createIndex(Indexes.ascending("timestamp"));
                logger.info(getClientName() + " creating index: host");
                coll.createIndex(Indexes.ascending("host"));
                logger.info(getClientName() + " creating index: ident");
                coll.createIndex(Indexes.ascending("ident"));
                dbIndexCreated = true;
                logger.info(getClientName() + " database initialized");
                AlarmMananger.clearAlarm(mongoDownAlarm);
            } catch (Exception e) {
                AlarmMananger.raiseAlarm(mongoDownAlarm);
                logger.error(getClientName() + " error initializing database: " + e.getMessage());
            }
        }

    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void addEvents(List<DEvent> eventList) {

        if (eventList == null || eventList.isEmpty()) return;

        logger.info(getClientName() + " addEvents: size=" + eventList.size());

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
//            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);
//            List<Document> list = new ArrayList<>();
//
//            for (DEvent e : eventList) {
//                Document doc = Document.parse(new Gson().toJson(e));
//                list.add(doc);
//                tryGrokFilter(e.getMessage()); // TODO why is this?
//            }
//            collection.insertMany(list, new InsertManyOptions().ordered(true));

            MongoCollection<DEvent> collection = db.getCollection(dbCollectionEvents, DEvent.class);
            collection.insertMany(eventList, new InsertManyOptions().ordered(true));

            AlarmMananger.clearAlarm(mongoDownAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
            logger.error(getClientName() + " addEvents: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "insert").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "insert").observe(duration);
        }
    }

    @Override
    public List<DEvent> getEvents(DFilter filter) {

        logger.info(getClientName() + " getEvents: " + filter);

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<DEvent> collection = db.getCollection(dbCollectionEvents, DEvent.class);

            List<DEvent> docsResultList;

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

            AlarmMananger.clearAlarm(mongoDownAlarm);

            return docsResultList;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
            logger.error(getClientName() + " getEvents: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "query").observe(duration);
        }
        return null;
    }

    public Bson prepareBsonFilter(DFilter filter) {

        List<Bson> tempBsonList = new ArrayList<>();

        if (filter.getHosts() != null && filter.getHosts().length > 0) {
            Bson[] bArray = new Bson[filter.getHosts().length];
            for (int i = 0; i < filter.getHosts().length; i++) {
                Bson b = Filters.eq("host", filter.getHosts()[i]);
                bArray[i] = b;
            }
            Bson hostsFilter = Filters.or(bArray);
            tempBsonList.add(hostsFilter);
        }

        if (filter.getIdents() != null && filter.getIdents().length > 0) {
            Bson[] bArray = new Bson[filter.getIdents().length];
            for (int i = 0; i < filter.getIdents().length; i++) {
                Bson b = Filters.eq("ident", filter.getIdents()[i]);
                bArray[i] = b;
            }
            Bson identsFilter = Filters.or(bArray);
            tempBsonList.add(identsFilter);
        }

        if (filter.getSearchPattern() != null && filter.getSearchPattern().length() > 0) {
            Bson b = Filters.regex("message", filter.getSearchPattern());
            tempBsonList.add(b);
            // https://www.mongodb.com/docs/manual/reference/operator/query/regex/
            //https://www.mongodb.com/docs/manual/reference/operator/query/regex/#std-label-syntax-restrictions
        }

        Bson b1 = Filters.gte("timestamp", filter.getFromTimestamp());
        Bson b2 = Filters.lte("timestamp", filter.getToTimestamp());
        tempBsonList.add(b1);
        tempBsonList.add(b2);

//        if (tempBsonList.isEmpty()) {
//            return null;
//        }
        Bson[] bArray = new Bson[tempBsonList.size()];
        tempBsonList.toArray(bArray);

        Bson bsonFilter;
        bsonFilter = Filters.and(bArray);
        logger.info(getClientName() + " BSON: " + bsonFilter);
        return bsonFilter;
    }

    @Override
    public Map<String, Integer> getTopEventsByHosts(int limit) {
        logger.info(getClientName() + " getTopEventsByHosts");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);

            // https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/aggregation/

            List<Document> docsResultList = collection.aggregate(
                            Arrays.asList(
                                    Aggregates.match(Filters.gte("timestamp", System.currentTimeMillis() - 4 * 3600 * 1000)),
                                    Aggregates.group("$host", Accumulators.sum("count", 1)),
                                    Aggregates.sort(Sorts.descending("count")),
                                    Aggregates.limit(limit)
                            )
                    ).into(new ArrayList<>());

            Map<String, Integer> map = new HashMap<>();
            for (Document d : docsResultList) {
                map.put(d.get("_id").toString(), Integer.parseInt(d.get("count").toString()));
//                System.out.println(d.toJson());
            }

            AlarmMananger.clearAlarm(mongoDownAlarm);

            return map;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
            logger.error(getClientName() + " getTopEventsByHosts: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "query").observe(duration);
        }
        return null;
    }

    @Override
    public Map<String, Integer> getTopEventsByIdent(int limit) {
        logger.info(getClientName() + " getTopEventsByIdent");

        long before = System.currentTimeMillis();

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(dbCollectionEvents);

            // https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/aggregation/

            List<Document> docsResultList = collection.aggregate(
                    Arrays.asList(
                            Aggregates.match(Filters.gte("timestamp", System.currentTimeMillis() - 4 * 3600 * 1000)),
                            Aggregates.group("$ident", Accumulators.sum("count", 1)),
                            Aggregates.sort(Sorts.descending("count")),
                            Aggregates.limit(limit)
                    )
            ).into(new ArrayList<>());

            Map<String, Integer> map = new HashMap<>();
            for (Document d : docsResultList) {
                map.put(d.get("_id").toString(), Integer.parseInt(d.get("count").toString()));
//                System.out.println(d.toJson());
            }

            AlarmMananger.clearAlarm(mongoDownAlarm);

            return map;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
            logger.error(getClientName() + " getTopEventsByIdent: Exception: " + e.getMessage());
            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "query").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "query").observe(duration);
        }
        return null;
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

            AlarmMananger.clearAlarm(mongoDownAlarm);

            return resultList;

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
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
            MongoCollection<Document> collection2 = db.getCollection(dbCollectionEvents);
            DeleteResult resultDeleteMany2 = collection2.deleteMany(filter);
            logger.info( getClientName() + " cleanDB [events]: size=" + resultDeleteMany2.getDeletedCount());

            AlarmMananger.clearAlarm(mongoDownAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(mongoDownAlarm);
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
