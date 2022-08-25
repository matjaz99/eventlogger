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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Formatter {

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

}