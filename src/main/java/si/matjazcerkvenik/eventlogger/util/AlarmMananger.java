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
package si.matjazcerkvenik.eventlogger.util;

import com.google.gson.Gson;
import okhttp3.*;
import si.matjazcerkvenik.eventlogger.db.ClientConfig;
import si.matjazcerkvenik.eventlogger.db.HttpClientFactory;
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AlarmMananger implements Runnable {

    private static SimpleLogger logger = LogFactory.getLogger();

    private static AlarmMananger instance;

    private OkHttpClient httpClient;

    private static Map<String, DAlarm> activeAlarmsList = new HashMap<>();

    /** This buffer is used to prevent blocking processing of events (because of timeouts when sending alarms) */
    private static ConcurrentLinkedQueue<DAlarm> alarmsBufferList = new ConcurrentLinkedQueue<>();

    private Thread t;
    private boolean running = true;

    public static AlarmMananger getInstance() {
        if (instance == null) instance = new AlarmMananger();
        return instance;
    }

    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(1000);

                if (alarmsBufferList.isEmpty()) continue;

                List<DAlarm> list = new ArrayList<>();
                while (!alarmsBufferList.isEmpty()) {
                    list.add(alarmsBufferList.poll());
                }

                String body = toJsonArrayString(list);
                push(body);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    public void stop() {
        running = false;
    }


    /**
     * Send alarm to alarm destination. Actually alarm will be added to buffer queue, and it will be sent
     * in a separate thread. If alarm was already sent, it is added to active alarm list,
     * and it will not be sent again.
     * @param alarm
     */
    public static synchronized void raiseAlarm(DAlarm alarm) {
        if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());
        alarm.setNotificationType("alarm");
        if (activeAlarmsList.containsKey(alarm.getAlarmId())) return;
        activeAlarmsList.put(alarm.getAlarmId(), alarm);
        alarmsBufferList.add(alarm);
    }

    /**
     * Clear active alarm. First check if alarm is present in active alarm list. If yes, then use the same
     * alarm, change the severity to clear and sent it (via buffer). Otherwise, nothing will happen.
     * @param alarm
     */
    public static synchronized void clearAlarm(DAlarm alarm) {
        DAlarm a = activeAlarmsList.remove(alarm.getAlarmId());
        if (a != null) {
            a.setSeverity(5);
            a.setTimestamp(System.currentTimeMillis());
            a.setNotificationType("clear");
            alarmsBufferList.add(a);
        }

    }

    /**
     * Event is sent to alarm destination. Events are not stored in active alarm list, and they are sent
     * on every occurrence.
     * @param alarm
     */
    public static void sendEvent(DAlarm alarm) {
        alarm.setTimestamp(System.currentTimeMillis());
        alarmsBufferList.add(alarm);
    }

    /**
     * Execute http post to send the event to destination.
     * @param body json formatted alarm
     */
    private void push(String body) {

        if (DProps.EVENTLOGGER_ALARM_DESTINATION == null) return;

        logger.info("AlarmMananger: push(): sending event: " + body);

        if (httpClient == null) {
            ClientConfig clientConfig = new ClientConfig(DProps.EVENTLOGGER_ALARM_DESTINATION);
            clientConfig.setConnectionTimeout(3);
            clientConfig.setReadTimeout(10);
            httpClient = HttpClientFactory.instantiateHttpClient(clientConfig);
        }

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        try {

            Request request = new Request.Builder()
                    .url(DProps.EVENTLOGGER_ALARM_DESTINATION)
                    .addHeader("User-Agent", "eventlogger/v1")
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = httpClient.newCall(request).execute();
            logger.info("AlarmMananger: push(): responseCode=" + response.code());

            response.close();

        } catch (Exception e) {
            logger.error("AlarmMananger: push(): Could not send event. " + e.getMessage());
        }

    }

    /**
     * Convert single alarm to JSON string.
     * @param alarm
     * @return json string
     */
    private String toJsonString(DAlarm alarm) {
        Gson gson = new Gson();
        String s = gson.toJson(alarm);
        return s;
    }

    /**
     * Convert a list of alarms to JSON string.
     * @param list
     * @return json string
     */
    private String toJsonArrayString(List<DAlarm> list) {
        Gson gson = new Gson();
        String s = gson.toJson(list);
        return s;
    }

    /**
     * Return complete list of active alarms as JSON string.
     * This method is called from API call (/alarms).
     * @return json string
     */
    public String toJsonStringAllActiveAlarms() {
        Gson gson = new Gson();
        String s = gson.toJson(activeAlarmsList.values());
        return s;
    }

}
