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

    private long timestamp = 0;
    private String dateTime;
    private String eventName = "Alarm incident";
    private int severity = 0;
    private String severityString = "Indeterminate";
    private String notificationType = "event";
    private String addInfo = "";
    /** IP of the server who sent the request (could be proxy or relay agent) */
    private String remoteHost;
    /** IP of server who originally generated the event */
    private String sourceHost;
    private String ident;
    private String tag;
    /** Original text message */
    private String message;

    public DAlarm() {}

    public DAlarm(String remoteHost, String sourceHost, String eventName, int severity, String ident,
                  String tag, String addInfo, String message) {
        this.remoteHost = remoteHost;
        this.sourceHost = sourceHost;
        this.eventName = eventName;
        this.addInfo = addInfo;
        setSeverity(severity);
        this.ident = ident;
        this.tag = tag;
        this.message = message;
    }


    public String getAlarmId() {
        return MD5Checksum.getMd5Checksum(sourceHost + eventName + ident + tag);
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



    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public String getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String addInfo) {
        this.addInfo = addInfo;
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
                ", eventName='" + eventName + '\'' +
                ", severity=" + severity +
                ", severityString='" + severityString + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", sourceHost='" + sourceHost + '\'' +
                ", ident='" + ident + '\'' +
                ", tag='" + tag + '\'' +
                ", customInfo='" + addInfo + '\'' +
                '}';
    }

}
