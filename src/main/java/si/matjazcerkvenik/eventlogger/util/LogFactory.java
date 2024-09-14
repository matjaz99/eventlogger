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

import java.io.File;

public class LogFactory {

    private static SimpleLogger logger = null;

    private static SimpleLogger incomingRequestsLog = null;

    private static SimpleLogger evgenLog = null;

//    {
//        getLogger(); // force calling getLogger just to initialize devEnv.
//    }

    public static SimpleLogger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger();
            if (DProps.DEV_ENV) {
                logger.setFilename("./eventlogger.log");
            }
        }
        return logger;
    }


    public static SimpleLogger getIncomingRequestsLog() {
        if (incomingRequestsLog == null) {
            incomingRequestsLog = new SimpleLogger();
            if (DProps.DEV_ENV) {
                logger.setFilename("./eventlogger-http-requests.log");
            } else {
                String f = logger.getFilename().replace("eventlogger.log", "eventlogger-http-requests.log");
                incomingRequestsLog.setFilename(f);
            }
            incomingRequestsLog.setLogLevel(LEVEL.DEBUG);
            incomingRequestsLog.setBackup(5);
            incomingRequestsLog.setMaxSizeMb(100);
            incomingRequestsLog.setVerbose(false);
            incomingRequestsLog.setFilePermissions("rw-r--r--");
            System.out.println(">>> logger req file 1: " + incomingRequestsLog.getFilename());
        }
        return incomingRequestsLog;
    }

    public static SimpleLogger getEvgenLog() {
        if (evgenLog == null) {
            evgenLog = new SimpleLogger();
            if (DProps.DEV_ENV) {
                logger.setFilename("./evgen-" + DProps.HOSTNAME + ".log");
            } else {
                String f = logger.getFilename().replace("eventlogger.log", "evgen-" + DProps.HOSTNAME + ".log");
                evgenLog.setFilename(f);
            }
            evgenLog.setLogLevel(LEVEL.DEBUG);
            evgenLog.setBackup(20);
            evgenLog.setMaxSizeMb(100);
            evgenLog.setFilePermissions("rw-r--r--");
        }
        return evgenLog;
    }

}
