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
package si.matjazcerkvenik.eventlogger.model;

import si.matjazcerkvenik.eventlogger.util.Formatter;
import si.matjazcerkvenik.eventlogger.util.MD5Checksum;

public class DAlarm {

    private String alarmId;
    private String alarmSource;
    private long timestamp = 0;
    private String dateTime;
    private String alarmName = "Alarm incident";
    private int severity = 0;
    private String severityString = "Indeterminate";
    private String notificationType = "alarm";
    private String sourceInfo = "";
    private String additionalInfo = "";

    public DAlarm() {}

    public DAlarm(String alarmSource, String alarmName, int severity, String sourceInfo, String additionalInfo) {
        this.alarmSource = alarmSource;
        this.alarmName = alarmName;
        setSeverity(severity);
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
    }

    public DAlarm(String alarmSource, String alarmName, int severity, String sourceInfo, String additionalInfo, boolean isEvent) {
        this.alarmSource = alarmSource;
        this.alarmName = alarmName;
        setSeverity(severity);
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
        if (isEvent) this.notificationType = "event";
    }


    public String getAlarmId() {
        alarmId = MD5Checksum.getMd5Checksum(alarmSource + alarmName + sourceInfo);
        return alarmId;
    }

    public String getAlarmSource() {
        alarmSource = "alarm source eventlogger";
        return alarmSource;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return Formatter.getFormatedTimestamp(timestamp);
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
        switch (this.severity) {
            case 1:
                severityString = "Critical";
                break;
            case 2:
                severityString = "Major";
                break;
            case 3:
                severityString = "Minor";
                break;
            case 4:
                severityString = "Warning";
                break;
            case 5:
                severityString = "Clear";
                break;
            case 6:
                severityString = "Informational";
                break;
            default:
                severityString = "Indeterminate";
        }
    }

    public String getSeverityString() {
        return severityString;
    }

    public String getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(String sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "alarmId='" + getAlarmId() + '\'' +
                ", timestamp=" + timestamp +
                ", alarmName='" + alarmName + '\'' +
                ", severity=" + severity +
                ", severityString='" + severityString + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", sourceInfo='" + sourceInfo + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }

}
