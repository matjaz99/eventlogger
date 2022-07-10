package si.matjazcerkvenik.eventlogger.util;

import si.matjazcerkvenik.simplelogger.SimpleLogger;

public class LogFactory {

    private static SimpleLogger logger = null;

    {
        getLogger(); // force calling getLogger just to initialize devEnv.
    }

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (logger.getFilename().contains("simple-logger.log")) {
                logger.setFilename("./djabeEventLogger.log");
            }
        }
        return logger;
    }


}
