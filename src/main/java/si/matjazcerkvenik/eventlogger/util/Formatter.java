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

import si.matjazcerkvenik.eventlogger.model.DEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Formatter {

    public static boolean isNullOrEmpty(String s) {
        if (s == null) return true;
        if (s.trim().isEmpty()) return true;
        return false;
    }

    /**
     * Format timestamp from millis into readable form.
     * @param timestamp timestamp in millis
     * @return readable date
     */
    public static String getFormatedTimestamp(long timestamp) {
        if (timestamp == 0) return "n/a";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(DProps.DATE_FORMAT);
        return sdf.format(cal.getTime());
    }

    /**
     * This will return given number of seconds in format: __d __h __m __s.
     * @param seconds
     * @return formatted time
     */
    public static String convertToDHMSFormat(int seconds) {
        int secRemain = seconds % 60;
        int minTotal = seconds / 60;
        int minRemain = minTotal % 60;
        int hourTotal = minTotal / 60;
        int hourRemain = hourTotal % 60;
        int dayTotal = hourTotal / 24;
        int dayRemain = hourTotal % 24;

        String resp = minRemain + "m " + secRemain + "s";

        if (dayTotal == 0) {
            if (hourRemain > 0) {
                resp = hourTotal + "h " + resp;
            }
        }

        if (dayTotal > 0) {
            resp = dayTotal + "d " + dayRemain + "h " + resp;
        }

        return resp;
    }

    public static String toNdJsonString(List<DEvent> eventList) {
        StringBuilder sb = new StringBuilder();

        for (DEvent e : eventList) {
            String msg = e.getMessage();
            msg = msg.replace("\"", "'");
            msg = msg.replace("\\", "\\\\");
//            String tag = "eventlogger"; // tag is index suffix
//            if (e.getTag() != null && e.getTag().length() > 0) {
//                tag += "-" + e.getTag();
//            }
            sb.append("{ \"index\":{\"_index\": \"eventlogger\"} }\n").append("{");
            sb.append("\"host\":\"").append(e.getHost()).append("\",");
            sb.append("\"ident\":\"").append(e.getIdent()).append("\",");
            sb.append("\"pid\":").append(e.getPid()).append(",");
            sb.append("\"tag\":\"").append(e.getTag()).append("\",");
            sb.append("\"message\":\"").append(msg).append("\",");
            sb.append("\"@timestamp\":").append(e.getTimestamp()).append("}\n");
        }

        return sb.toString();

    }

}
