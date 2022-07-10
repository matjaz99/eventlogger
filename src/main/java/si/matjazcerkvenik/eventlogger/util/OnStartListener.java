package si.matjazcerkvenik.eventlogger.util;


import si.matjazcerkvenik.eventlogger.web.DelMetrics;

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
        DelProps.START_UP_TIMESTAMP = System.currentTimeMillis();

        // read version file
        InputStream inputStream = servletContextEvent.getServletContext().getResourceAsStream("/WEB-INF/version.txt");
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            DelProps.VERSION = br.readLine().trim();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DelProps.LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            DelProps.LOCAL_IP = "UnknownHost";
        }

        LogFactory.getLogger().info("\n");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("*             EventLogger started              *");
        LogFactory.getLogger().info("*                                              *");
        LogFactory.getLogger().info("************************************************");
        LogFactory.getLogger().info("");
        LogFactory.getLogger().info("VERSION=" + DelProps.VERSION);
        LogFactory.getLogger().info("LOCAL_IP=" + DelProps.LOCAL_IP);

        DelProps.RUNTIME_ID = UUID.randomUUID().toString();
        LogFactory.getLogger().info("RUNTIME_ID=" + DelProps.RUNTIME_ID);

        // Don't call DAO before reading env vars!!!

        // read all environment variables
        LogFactory.getLogger().info("***** Environment variables *****");
        Map<String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            LogFactory.getLogger().info(entry.getKey() + "=" + entry.getValue());
        }

        // TODO load config file

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
            DelProps.IS_CONTAINERIZED = true;
        } catch (IOException e) {
            LogFactory.getLogger().info("Running in container: false");
            DelProps.IS_CONTAINERIZED = false;
        }

        DelMetrics.eventlogger_build_info.labels("EventLogger", DelProps.RUNTIME_ID, DelProps.VERSION, System.getProperty("os.name")).set(DelProps.START_UP_TIMESTAMP);

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("STOPPED");
    }
}
