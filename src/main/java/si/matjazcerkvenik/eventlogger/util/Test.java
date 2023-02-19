package si.matjazcerkvenik.eventlogger.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import si.matjazcerkvenik.eventlogger.model.DEvent;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {

        tryRegex();
        tryGrok();

        testDb1();

    }

    private static void tryGrok() {

        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();

//        String line = "time=\"2022-08-06T15:04:49.297937545+02:00\" level=error msg=\"error sending message to peer\" error=\"rpc error: code = Unavailable desc = connection error: desc = \\\"transport: Error while dialing dial tcp 192.168.0.191:2377: connect: no route to host\\\"\"";
//        String pattern = "error"; // not found
//        String pattern = "%{GREEDYDATA}error%{GREEDYDATA}"; // found
//        String pattern = "(.*)\\{error\\}(.*)"; // not found
//        String pattern = "^(.*)\b(error)\b(.*)$"; // not found
//        String pattern = ".*(?>error).*"; // not found
//        String pattern = "(?<name0>.*)error(?<name1>.*)"; // found; the same as GREEDYDATA

        String line = "New session 7 of user root.";
        String pattern = "New session %{INT} of user root."; // found

        final Grok grok = grokCompiler.compile(pattern);
        Match gm = grok.match(line);
        System.out.println("GROK PATTERN: " + grok.getNamedRegex());
        final Map<String, Object> capture = gm.capture();
        if (capture.size() == 0) {
            System.out.println("grok nothing found");
            return;
        }
        for (String s : capture.keySet()) {
            System.out.println("GROK RESULT: " + capture.get(s).toString());
        }

    }

    private static void tryRegex() {

        String line = "CMDOUT ERROR (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
        String pattern = "(?i)error"; // found

//        String line = "New session 7 of user root.";
//        String pattern = "New session \\d+ of user root"; // found

//        String line = "(root) CMDOUT (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
//        String pattern = "command not found"; // found

//        String line = "(root) CMDOUT (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
//        String pattern = "command not found"; // found

        Pattern patternObj = Pattern.compile(pattern);
        System.out.println("REGEX PATTERN: " + patternObj.toString());
        Matcher matcher = patternObj.matcher(line);
        boolean matchFound = matcher.find();
        if (matchFound) {
            System.out.println("regex match found");
        } else {
            System.out.println("regex match not found");
        }

    }




    /*           MongoDB           */


    public static void testDb1() {

        https://www.mongodb.com/developer/languages/java/java-mapping-pojos/

        DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@lionvm:27017/test?authSource=admin";

        ConnectionString connectionString = new ConnectionString(DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);

        List<Document> databases = mongoClient.listDatabases().into(new ArrayList<>());
        databases.forEach(db -> System.out.println(db.toJson()));

        MongoDatabase db = mongoClient.getDatabase("testdb");
        MongoCollection<DEvent> eventsCollection = db.getCollection("events", DEvent.class);

        DEvent eventIn = new DEvent();
        eventIn.setId(DProps.eventsReceivedCount++);
        eventIn.setRuntimeId("1000");
        eventIn.setTimestamp(System.currentTimeMillis());
        eventIn.setEventSource("test_source");
        eventIn.setEndpoint("/foo");
        eventIn.setHost("10.20.30.40");
        eventIn.setIdent("my_process");

        // insert one
        eventsCollection.insertOne(eventIn);

        // read
        DEvent eventOut = eventsCollection.find(Filters.eq("eventSource", "test_source")).first();
        System.out.println("Found: " + eventOut.toString());

        // insert many
        List<DEvent> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DEvent ev = new DEvent();
            ev.setId(DProps.eventsReceivedCount++);
            ev.setRuntimeId("1001");
            ev.setTimestamp(System.currentTimeMillis());
            ev.setEventSource("test_many_source");
            ev.setEndpoint("/bar");
            ev.setHost("10.20.30.40");
            ev.setIdent("insert_many");
            list.add(ev);
        }
        eventsCollection.insertMany(list);

        DEvent event2 = eventsCollection.find(Filters.eq("_id", "63f131fb3ab56b057feb251e ")).first();
        System.out.println("Found: " + event2.toString());


//        database.listCollectionNames().forEach(System.out::println);

//        MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("grades");

    }
}
