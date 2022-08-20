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
package si.matjazcerkvenik.eventlogger.web;

import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.Formatter;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class ConfigBean {

    public Integer getDataRetention() {
        return DProps.EVENTLOGGER_DATA_RETENTION_DAYS;
    }

    public void setDataRetention(Integer i) {
        try {
            DProps.EVENTLOGGER_DATA_RETENTION_DAYS = i;
            LogFactory.getLogger().info("ConfigBean: set EVENTLOGGER_DATA_RETENTION_DAYS: " + DProps.EVENTLOGGER_DATA_RETENTION_DAYS);
        } catch (NumberFormatException e) {
            LogFactory.getLogger().error("ConfigBean: set EVENTLOGGER_DATA_RETENTION_DAYS failed: " + e.getMessage());
        }
    }

    public Integer getBufferSize() {
        return DProps.EVENTLOGGER_MEMORY_BUFFER_SIZE;
    }

    public void setBufferSize(Integer i) {
        try {
            DProps.EVENTLOGGER_MEMORY_BUFFER_SIZE = i;
            LogFactory.getLogger().info("ConfigBean: set EVENTLOGGER_MEMORY_BUFFER_SIZE: " + DProps.EVENTLOGGER_MEMORY_BUFFER_SIZE);
        } catch (NumberFormatException e) {
            LogFactory.getLogger().error("ConfigBean: set EVENTLOGGER_MEMORY_BUFFER_SIZE failed: " + e.getMessage());
        }
    }

    public long getEventsCount() {
        return DProps.eventsReceivedCount;
    }

    public String getRuntimeId() {
        return DProps.RUNTIME_ID;
    }

    public String getUpTime() {
        int secUp = (int) ((System.currentTimeMillis() - DProps.START_UP_TIMESTAMP) / 1000);
        return Formatter.convertToDHMSFormat(secUp);
    }

    public String getVersion() {
        return DProps.VERSION;
    }

    public boolean isContainerized() {
        return DProps.IS_CONTAINERIZED;
    }

    public String getLocalIp() {
        return DProps.LOCAL_IP;
    }

    public String getStorageType() {
        return DProps.EVENTLOGGER_STORAGE_TYPE;
    }

    public String getMongoConnectionString() {
        if (DProps.EVENTLOGGER_STORAGE_TYPE.equalsIgnoreCase("mongodb")) {
            String s = DProps.EVENTLOGGER_MONGODB_CONNECTION_STRING;
            // mask password
            String[] a1 = s.split("://");
            if (a1[1].contains("@")) {
                String[] a2 = a1[1].split("@");
                if (a2[0].contains(":")) {
                    String[] a3 = a2[0].split(":");
                    s = s.replace(a3[0] + ":" + a3[1] + "@",
                            a3[0] + ":" + "•••••••••" + "@");
                }
            }
            return s;
        }
        return "-";
    }

    public int getDbPoolSize() {
        return DProps.EVENTLOGGER_DB_POOL_SIZE;
    }

}
