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
import si.matjazcerkvenik.eventlogger.model.DAlarm;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;

public class AlarmMananger {

    private static SimpleLogger logger = LogFactory.getLogger();

    private static Map<String, DAlarm> activeAlarmsList = new HashMap<>();


    public static synchronized void raiseAlarm(DAlarm alarm) {
        if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());
        if (alarm.getNotificationType().equalsIgnoreCase("alarm")) {
            if (activeAlarmsList.containsKey(alarm.getAlarmId())) return;
            activeAlarmsList.put(alarm.getAlarmId(), alarm);
        }
        String body = toJsonString(alarm);
        push(body, (alarm.getSeverity() == 5 ? "CLEAR" : "ALARM"));
    }

    public static synchronized void clearAlarm(DAlarm alarm) {
        DAlarm a = activeAlarmsList.remove(alarm.getAlarmId());
        if (a != null) {
            a.setSeverity(5);
            alarm.setTimestamp(System.currentTimeMillis());
            String body = toJsonString(alarm);
            push(body, (alarm.getSeverity() == 5 ? "CLEAR" : "ALARM"));
        }
    }

    public static void sendEvent(DAlarm alarm) {
        alarm.setTimestamp(System.currentTimeMillis());
        alarm.setNotificationType("event");
        String body = toJsonString(alarm);
        push(body, "EVENT");
    }

    /**
     * Execute http post to send the event to destination.
     * @param body json formatted alarm
     * @param eventType could be ALARM, CLEAR or EVENT
     */
    private static void push(String body, String eventType) {

        if (DProps.EVENTLOGGER_ALARM_DESTINATION == null) return;

        logger.info("AlarmMananger: push(): sending " + eventType + ": " + body);

        OkHttpClient httpClient = new OkHttpClient();
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
            logger.error("AlarmMananger: push(): Could not send alarm. " + e.getMessage());
        }

    }

    public static String toJsonString(DAlarm alarm) {
        Gson gson = new Gson();
        String s = gson.toJson(alarm);
        return s;
    }

    public static String toJsonStringAllAlarms() {
        Gson gson = new Gson();
        String s = gson.toJson(activeAlarmsList.values());
        return s;
    }

}
