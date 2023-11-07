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


import si.matjazcerkvenik.eventlogger.model.config.YamlConfig;

public class DProps {

    // internal counters
    private static long requestsReceivedCount = 0;

    public static synchronized long increaseAndGetRequestsReceivedCount() {
        return requestsReceivedCount++;
    }

    private static long eventsReceivedCount = 0;

    public static synchronized long increaseAndGetEventsReceivedCount() {
        return eventsReceivedCount++;
    }

    public static long getEventsReceivedCount() {
        return eventsReceivedCount;
    }

    public static YamlConfig yamlConfig;

    public static String RUNTIME_ID = "0000-0000-0000-0000";
    public static long START_UP_TIMESTAMP = 0;
    public static String VERSION = "n/a";
    public static boolean IS_CONTAINERIZED = false;
    public static String LOCAL_IP;
    public static String DATE_FORMAT = "yyyy/MM/dd H:mm:ss";
    public static String EVENTLOGGER_EVENT_RULES_CONFIG_FILE = "/opt/eventlogger/event_rules.yml";

    // memory or mongodb
    public static String EVENTLOGGER_STORAGE_TYPE = "mongodb";
    public static int EVENTLOGGER_MEMORY_BUFFER_SIZE = 1000;
    public static int EVENTLOGGER_DATA_RETENTION_DAYS = 30;
    public static int EVENTLOGGER_DB_POOL_SIZE = 3;

    // mongodb config
    public static String EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:password@hostname:27017/test?authSource=admin";
    public static int EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC = 5;
    public static int EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC = 30;

    // opensearch config
    public static String EVENTLOGGER_OPENSEARCH_CONNECTION_STRING = "https://admin:admin@hostname:9200";
    public static int EVENTLOGGER_OPENSEARCH_CONNECT_TIMEOUT_SEC = 5;
    public static int EVENTLOGGER_OPENSEARCH_READ_TIMEOUT_SEC = 30;
    public static String EVENTLOGGER_OPENSEARCH_INDEX_NAME = "eventlogger";

    public static String EVENTLOGGER_ALARM_DESTINATION = "http://alertmonitor:8080/alerts";

}
