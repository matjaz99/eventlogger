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
    private long timestamp = 0;
    private String dateTime;
    private String alarmName = "Alarm incident";
    private int severity = 0;
    private String severityString = "Indeterminate";
    private String notificationType = "event";
    private String customInfo = "";
    /** IP of the server who sent the request (could be proxy or relay agent) */
    private String remoteHost;
    /** IP of server who originally generated the event */
    private String sourceHost;
    private String ident;
    private String tag;
    /** Original text message */
    private String message;

    public DAlarm() {}

    public DAlarm(String remoteHost, String sourceHost, String alarmName, int severity, String ident, String tag, String message) {
        this.remoteHost = remoteHost;
        this.sourceHost = sourceHost;
        this.alarmName = alarmName;
        setSeverity(severity);
        this.ident = ident;
        this.tag = tag;
        this.message = message;
    }


    public String getAlarmId() {
        alarmId = MD5Checksum.getMd5Checksum(sourceHost + alarmName + ident + tag + message);
        return alarmId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        dateTime = Formatter.getFormatedTimestamp(timestamp);
    }

    public String getDateTime() {
        return dateTime;
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

    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DAlarm{" +
                "alarmId='" + getAlarmId() + '\'' +
                ", timestamp=" + timestamp +
                ", alarmName='" + alarmName + '\'' +
                ", severity=" + severity +
                ", severityString='" + severityString + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", sourceHost='" + sourceHost + '\'' +
                ", ident='" + ident + '\'' +
                ", tag='" + tag + '\'' +
                ", customInfo='" + customInfo + '\'' +
                '}';
    }

}
