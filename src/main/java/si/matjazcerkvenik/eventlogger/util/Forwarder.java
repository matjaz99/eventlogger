package si.matjazcerkvenik.eventlogger.util;

import okhttp3.*;
import si.matjazcerkvenik.eventlogger.db.HttpClientFactory;
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

/**
 * Send event to HTTP endpoint (destination)
 */
public class Forwarder {

    private static SimpleLogger logger = LogFactory.getLogger();

    public static void push(DAlarm dAlarm, String connectionString) {

        String body = Formatter.toJsonString(dAlarm);

        logger.info("Forwarder: push(): sending event: " + body);

        OkHttpClient httpClient = HttpClientFactory.instantiateHttpClient(connectionString);

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        try {

            Request request = new Request.Builder()
                    .url(connectionString)
                    .addHeader("User-Agent", "eventlogger/v1")
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = httpClient.newCall(request).execute();
            logger.info("Forwarder: push(): responseCode=" + response.code());

            response.close();

        } catch (Exception e) {
            logger.error("Forwarder: push(): Could not send event. " + e.getMessage());
        }

    }

}
