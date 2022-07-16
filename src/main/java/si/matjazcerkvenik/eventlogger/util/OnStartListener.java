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

        // Don't call DAO before reading env vars!!!

        // read all environment variables
        LogFactory.getLogger().info("***** Environment variables *****");
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            LogFactory.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        DProps.EVENTLOGGER_STORAGE_TYPE = System.getenv().getOrDefault("EVENTLOGGER_STORAGE_TYPE", "memory").trim();
        DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING = System.getenv().getOrDefault("EVENTLOGGER_MONGODB_CONNECTION_STRING", "mongodb://admin:mongodbpassword@promvm:27017/?authSource=admin").trim();
        DProps.EVENTLOGGER_DATA_RETENTION_DAYS = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_DATA_RETENTION_DAYS", "30").trim());
        DProps.EVENTLOGGER_DB_POOL_SIZE = Integer.parseInt(System.getenv().getOrDefault("EVENTLOGGER_DB_POOL_SIZE", "10").trim());

        // runtime memory info
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        LogFactory.getLogger().info("***** Heap utilization statistics [MB] *****");
        LogFactory.getLogger().info("Total Memory: " + instance.totalMemory() / mb); // available memory
        LogFactory.getLogger().info("Free Memory: " + instance.freeMemory() / mb); // free memory
        LogFactory.getLogger().info("Used Memory: "
                + (instance.totalMemory() - instance.freeMemory()) / mb); // used memory
        LogFactory.getLogger().info("Max Memory: " + instance.maxMemory() / mb); // Maximum available memory

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

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        TaskManager.getInstance().stopDbMaintenanceTimer();
    }
}
