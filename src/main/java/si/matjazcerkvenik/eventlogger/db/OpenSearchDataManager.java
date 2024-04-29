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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import okhttp3.*;
import si.matjazcerkvenik.eventlogger.model.*;
import si.matjazcerkvenik.eventlogger.util.*;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpenSearchDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();
    private int clientId = 0;
    private String clientName;

    private OkHttpClient httpClient;
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    private ClientConfig clientConfig;
    private static long requestCount = 0;

    private static DAlarm opensearchDownAlarm = new DAlarm("eventlogger", "OpenSearch down",
            DAlarmSeverity.CRITICAL,
            DProps.EVENTLOGGER_OPENSEARCH_CONNECTION_STRING,
            "Cannot connect to OpenSearch");

    public OpenSearchDataManager(int id) {
        this.clientId = clientId;
        clientName = "OpenSearch[" + clientId + "]";

        clientConfig = new ClientConfig(DProps.EVENTLOGGER_OPENSEARCH_CONNECTION_STRING);
        clientConfig.setConnectionTimeout(DProps.EVENTLOGGER_OPENSEARCH_CONNECT_TIMEOUT_SEC);
        clientConfig.setReadTimeout(DProps.EVENTLOGGER_OPENSEARCH_READ_TIMEOUT_SEC);

        httpClient = HttpClientFactory.instantiateHttpClient(clientConfig);

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

            Request request = new Request.Builder()
                    .url(clientConfig.getConnectionString() + "/" + DProps.EVENTLOGGER_OPENSEARCH_INDEX_NAME + "/_bulk")
                    .addHeader("User-Agent", "OkHttp")
//                .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(Formatter.toNdJsonString(eventList), MEDIA_TYPE_JSON))
                    .build();

            System.out.println(Formatter.toNdJsonString(eventList));

            execute(request);

            AlarmMananger.clearAlarm(opensearchDownAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(opensearchDownAlarm);
            logger.error(clientName + " addEvents: Exception: " + e.getMessage());
//            DMetrics.eventlogger_db_errors_total.labels(dbName, dbCollectionEvents, "insert").inc();
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
//            DMetrics.eventlogger_db_duration_seconds.labels(dbName, dbCollectionEvents, "insert").observe(duration);
        }
    }

    @Override
    public List<DEvent> getEvents(DFilter filter) {

        logger.info(getClientName() + " getEvents: " + filter);

        try {

            String query = "{\n" +
                    "    \"size\": 1000,\n" +
                    "    \"query\": {\n" +
                    "        \"match_all\": {}\n" +
                    "    },\n" +
                    "    \"sort\": [\n" +
                    "        { \"@timestamp\": \"asc\" }\n" +
                    "    ]\n" +
                    "}";

            Request request = new Request.Builder()
                    .url(clientConfig.getConnectionString() + "/" + DProps.EVENTLOGGER_OPENSEARCH_INDEX_NAME + "/_search")
                    .addHeader("User-Agent", "OkHttp")
//                .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(query, MEDIA_TYPE_JSON))
                    .build();

            execute(request);

            if (filter == null) {

            } else {

            }

            AlarmMananger.clearAlarm(opensearchDownAlarm);

        } catch (Exception e) {
            AlarmMananger.raiseAlarm(opensearchDownAlarm);
            logger.error(clientName + " getEvents: Exception: " + e.getMessage());
        } finally {

        }

        return null;
    }

    @Override
    public Map<String, Integer> getTopEventsByHosts(int limit) {
        return null;
    }

    @Override
    public Map<String, Integer> getTopEventsByIdent(int limit) {
        return null;
    }

    @Override
    public List<String> getDistinctKeys(String key) {
        return null;
    }


    private String execute(Request request) throws ClientException {

        long reqID = requestCount++;

        String responseBody = null;
        long before = System.currentTimeMillis();
        String code = "0";

        try {

            logger.info(clientName + ": req[" + reqID + "] >> " + request.method().toUpperCase() + " " + request.url());
            Response response = httpClient.newCall(request).execute();
            logger.info(clientName + ": req[" + reqID + "] << code=" + response.code() + ", success=" + response.isSuccessful());

            code = Integer.toString(response.code());

            if (response.body() != null && response.code()/100 == 2) {
                responseBody = response.body().string();
                logger.trace(clientName + ": req[" + reqID + "] body: " + responseBody);
            }

            response.close();

        } catch (UnknownHostException e) {
            logger.error(clientName + ": req[" + reqID + "] << UnknownHostException: " + e.getMessage());
            code = "0";
            throw new ClientException("Unknown Host");
        } catch (SocketTimeoutException e) {
            logger.error(clientName + ": req[" + reqID + "] << SocketTimeoutException: " + e.getMessage());
            code = "0";
            throw new ClientException("Timeout");
        } catch (SocketException e) {
            logger.error(clientName + ": req[" + reqID + "] << SocketException: " + e.getMessage());
            code = "0";
            throw new ClientException("Socket Error");
        } catch (SSLException e) {
            logger.error(clientName + ": req[" + reqID + "] << SSLException: " + e.getMessage());
            code = "0";
            throw new ClientException("SSL Exception");
        } catch (Exception e) {
            logger.error(clientName + ": req[" + reqID + "] << Exception: ", e);
            code = "0";
            throw new ClientException("Unknown Exception");
        } finally {
            double duration = (System.currentTimeMillis() - before) * 1.0 / 1000;
//            AmMetrics.alertmonitor_prom_api_duration_seconds.labels(name, request.method(), code, request.url().toString()).observe(duration);
        }

        return responseBody;

    }

    @Override
    public void cleanDB() {
        // not applicable, ISM manages lifecycle of indices
    }

    @Override
    public void close() {

    }
}
