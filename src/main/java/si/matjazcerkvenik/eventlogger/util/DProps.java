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


public class DProps {

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
