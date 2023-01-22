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
            int sev = a.getSeverity();
            a.setSeverity(5);
            if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());
            String body = toJsonString(alarm);
            push(body, (alarm.getSeverity() == 5 ? "CLEAR" : "ALARM"));
            a.setSeverity(sev);
            a.setTimestamp(0);
        }
        alarm.setTimestamp(0);
    }

    private static void push(String body, String severity) {

        if (DProps.EVENTLOGGER_ALARM_DESTINATION == null) return;

        logger.info("push(): sending " + severity + ": " + body);

        OkHttpClient httpClient = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        try {

            Request request = new Request.Builder()
                    .url(DProps.EVENTLOGGER_ALARM_DESTINATION)
                    .addHeader("User-Agent", "eventlogger/v1")
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = httpClient.newCall(request).execute();
//            logger.info("push(): responseCode=" + response.code());
            boolean success = response.isSuccessful();
            int responseCode = response.code();
            String responseText = response.body().string();
            response.close();

        } catch (Exception e) {
            logger.error("push(): Could not send alarm. " + e.getMessage());
        }

    }

    public static void main(String... args) {
        DAlarm a1 = new DAlarm();
        a1.setAlarmName("alarm name");
        a1.setSeverity(1);
        a1.setSourceInfo("source info");

        DAlarm a2 = new DAlarm();
        a2.setAlarmName("alarm name 2");
        a2.setSeverity(2);
        a2.setSourceInfo("source info 2");

//        toJsonString(a1);

        List<DAlarm> list = new ArrayList<>();
        list.add(a1);
        list.add(a2);

        Gson gson = new Gson();
        String s = gson.toJson(list);
        System.out.println(s);
    }

    public static String toJsonString(DAlarm alarm) {
        Gson gson = new Gson();
        String s = gson.toJson(alarm);
        System.out.println(s);
        return s;
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return "[" + mapper.writeValueAsString(alarm) + "]";
//        } catch (JsonProcessingException e) {
//            logger.error("JsonProcessingException: " + e.getMessage());
//        }
//        return null;
    }

    public static String toJsonStringAllAlarms() {
        Gson gson = new Gson();
        String s = gson.toJson(activeAlarmsList.values());
        System.out.println(s);
        return s;
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return mapper.writeValueAsString(activeAlarmsList.values());
//        } catch (JsonProcessingException e) {
//            logger.error("JsonProcessingException: " + e.getMessage());
//        }
//        return null;
    }

}
