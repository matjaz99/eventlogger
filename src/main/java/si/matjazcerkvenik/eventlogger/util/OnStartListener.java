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


import si.matjazcerkvenik.eventlogger.model.config.ConfigReader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class OnStartListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        DProps.START_UP_TIMESTAMP = System.currentTimeMillis();

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            DProps.VERSION = br.readLine().trim();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DProps.LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            DProps.LOCAL_IP = "UnknownHost";
        }

        LogFactory.getLogger().info("\n");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("*             EventLogger started              *");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("");
        LogFactory.getLogger().info("VERSION=" + DProps.VERSION);
        LogFactory.getLogger().info("LOCAL_IP=" + DProps.LOCAL_IP);

        DProps.RUNTIME_ID = UUID.randomUUID().toString();
        LogFactory.getLogger().info("RUNTIME_ID=" + DProps.RUNTIME_ID);

        // read all environment variables
        LogFactory.getLogger().info("***** Environment variables *****");
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            LogFactory.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        // read application specific environment variables
        DProps.EVENTLOGGER_STORAGE_TYPE = System.getenv().getOrDefault("EVENTLOGGER_STORAGE_TYPE", "memory").trim();
        DProps.EVENTLOGGER_MEMORY_BUFFER_SIZE = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_MEMORY_BUFFER_SIZE", "1000").trim());
        DProps.EVENTLOGGER_DATA_RETENTION_DAYS = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_DATA_RETENTION_DAYS", "30").trim());
        DProps.EVENTLOGGER_DB_POOL_SIZE = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_DB_POOL_SIZE", "3").trim());
        DProps.EVENTLOGGER_GUI_DISPLAY_PATTERN = System.getenv().getOrDefault("EVENTLOGGER_GUI_DISPLAY_PATTERN", "%D - %h - %i[%p] - %t - %m").trim();
        DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("EVENTLOGGER_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@mongovm:27017/?authSource=admin").trim();
        DProps.EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC", "5").trim());
        DProps.EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC", "30").trim());
        DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC", "0").trim());
        DProps.EVENTLOGGER_MONGODB_BATCH_INSERT_MAX_SIZE = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_MONGODB_BATCH_INSERT_MAX_SIZE", "500").trim());
        DProps.EVENTLOGGER_OPENSEARCH_CONNECTION_STRING = System.getenv().getOrDefault("EVENTLOGGER_OPENSEARCH_CONNECTION_STRING", "https://admin:admin@hostname:9200").trim();
        DProps.EVENTLOGGER_OPENSEARCH_CONNECT_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_OPENSEARCH_CONNECT_TIMEOUT_SEC", "5").trim());
        DProps.EVENTLOGGER_OPENSEARCH_READ_TIMEOUT_SEC = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_OPENSEARCH_READ_TIMEOUT_SEC", "30").trim());
        DProps.EVENTLOGGER_OPENSEARCH_INDEX_NAME = System.getenv().getOrDefault("EVENTLOGGER_OPENSEARCH_INDEX_NAME", "eventlogger").trim();
        DProps.EVENTLOGGER_ALARM_DESTINATION = System.getenv().getOrDefault("EVENTLOGGER_ALARM_DESTINATION", "http://alertmonitor:8080/alertmonitor/alerts").trim();
        DProps.EVENTLOGGER_EVENT_RULES_CONFIG_FILE = System.getenv().getOrDefault("EVENTLOGGER_EVENT_RULES_CONFIG_FILE", "/opt/eventlogger/rules/event_rules.yml").trim();
        DProps.EVENTLOGGER_EVENT_RULES_LAST_RESET_TIME = Formatter.getFormatedTimestamp(System.currentTimeMillis());

        // set development environment
        if (new File("/Users/matjaz").exists()) {
            LogFactory.getLogger().warn("#######   RUNNING IN DEV MODE   #######");
//            DProps.EVENTLOGGER_STORAGE_TYPE = "memory";
            DProps.EVENTLOGGER_STORAGE_TYPE = "mongodb";
//            DProps.EVENTLOGGER_STORAGE_TYPE = "opensearch";
            DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@ubuntu-vm:27017/?authSource=admin";
//            DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@lionvm:27017/?authSource=admin";
//            DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@localhost:27017/?authSource=admin";
            DProps.EVENTLOGGER_MONGODB_FLUSH_INTERVAL_SEC = 15;
            DProps.EVENTLOGGER_MONGODB_BATCH_INSERT_MAX_SIZE = 10;
            DProps.EVENTLOGGER_OPENSEARCH_CONNECTION_STRING = "https://admin:admin@elasticvm:9200";
            DProps.EVENTLOGGER_DATA_RETENTION_DAYS = 500;
            DProps.EVENTLOGGER_DB_POOL_SIZE = 10;
            DProps.EVENTLOGGER_ALARM_DESTINATION = "http://192.168.0.25:7070/alertmonitor/webhook/eventlogger";
            DProps.EVENTLOGGER_EVENT_RULES_CONFIG_FILE = "rules/event_rules.yml";
        }

        // is running inside docker
        try (Stream< String > stream =
                     Files.lines(Paths.get("/proc/1/cgroup"))) {
            LogFactory.getLogger().info("Running in container: " + stream.anyMatch(line -> line.contains("/docker")));
            DProps.IS_CONTAINERIZED = true;
        } catch (IOException e) {
            LogFactory.getLogger().info("Running in container: false");
            DProps.IS_CONTAINERIZED = false;
        }

        DMetrics.eventlogger_build_info.labels("EventLogger", DProps.RUNTIME_ID, DProps.VERSION, System.getProperty("os.name")).set(DProps.START_UP_TIMESTAMP);

        // load yaml config file
        DProps.yamlConfig = ConfigReader.loadProvidersYamlConfig(DProps.EVENTLOGGER_EVENT_RULES_CONFIG_FILE);

        TaskManager.getInstance().startFlushQueueTimer();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        TaskManager.getInstance().stopDbMaintenanceTimer();
        TaskManager.getInstance().stopFlushQueueTimer();
    }
}
