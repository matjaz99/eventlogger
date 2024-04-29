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

import si.matjazcerkvenik.simplelogger.LEVEL;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

public class LogFactory {

    private static SimpleLogger logger = null;

    private static SimpleLogger incomingRequestsLog = null;

    {
        getLogger(); // force calling getLogger just to initialize devEnv.
    }

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (logger.getFilename().contains("simple-logger.log")) {
                logger.setFilename("./eventlogger.log");
            }
        }
        return logger;
    }


    public static SimpleLogger getIncomingRequestsLog() {
        if (incomingRequestsLog == null) {
            incomingRequestsLog = new SimpleLogger();
            incomingRequestsLog.setFilename("./eventlogger-http-requests.log");
            incomingRequestsLog.setLogLevel(LEVEL.DEBUG);
            incomingRequestsLog.setBackup(5);
            incomingRequestsLog.setMaxSizeMb(100);
            incomingRequestsLog.setVerbose(false);
        }
        return incomingRequestsLog;
    }

}
