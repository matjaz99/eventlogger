package si.matjazcerkvenik.eventlogger.util;


public class DelProps {

    // internal counters
    public static long webhookMessagesReceivedCount = 0;

    public static String RUNTIME_ID = "0000-0000-0000-0000";
    public static long START_UP_TIMESTAMP = 0;
    public static String VERSION = "n/a";
    public static boolean IS_CONTAINERIZED = false;
    public static String LOCAL_IP;

    public static String EVENTLOGGER_STORAGE_TYPE = "MEMORY";
    public static String EVENTLOGGER_MONGODB_CONNECTION_STRING = "mongodb://admin:mongodbpassword@promvm:27017/test?authSource=admin";

}
