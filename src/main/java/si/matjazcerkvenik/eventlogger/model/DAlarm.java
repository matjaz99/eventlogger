package si.matjazcerkvenik.eventlogger.model;

import si.matjazcerkvenik.eventlogger.util.DProps;
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
        this.severity = severity;
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
    }

    public DAlarm(String alarmSource, String alarmName, int severity, String sourceInfo, String additionalInfo, boolean isEvent) {
        this.alarmSource = alarmSource;
        this.alarmName = alarmName;
        this.severity = severity;
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
    }

    public String getSeverityString() {
        switch (severity) {
            case 1:
                return "Critical";
            case 2:
                return "Major";
            case 3:
                return "Minor";
            case 4:
                return "Warning";
            case 5:
                return "Clear";
            case 6:
                return "Informational";
        }
        return "Indeterminate";
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
