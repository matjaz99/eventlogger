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

import org.bson.codecs.pojo.annotations.BsonProperty;

public class DEvent {

    /** Just a counter; starts from zero on restart */
    @BsonProperty(value = "id")
    private long id;
    /** Runtime ID; uniqueness is guaranteed together with ID */
    @BsonProperty(value = "runtimeId")
    private String runtimeId;
    /** Unix timestamp in millis when the event was received */
    @BsonProperty(value = "timestamp")
    private long timestamp;
    /** The IP address of server which sent the http request */
    @BsonProperty(value = "remoteAddress")
    private String remoteAddress;
    /** Hostname or IP of object which sent the original event */
    @BsonProperty(value = "host")
    private String host;
    /** Process name that generated event */
    @BsonProperty(value = "ident")
    private String ident;
    /** PID of the process that generated event */
    @BsonProperty(value = "pid")
    private String pid;
    /** Event tag, usually appended by another agent */
    @BsonProperty(value = "tag")
    private String tag;
    /** Main message body as it was received, could be plaintext or raw json format. */
    @BsonProperty(value = "message")
    private String message;
    /** The source IP address of server who generated the event */
    @BsonProperty(value = "eventSource")
    private String eventSource;
    /** Typically a log file where event/line originates from. */
    @BsonProperty(value = "logfile")
    private String logfile;
    /** Eventlogger endpoint where event was received -> parser which processed the event */
    @BsonProperty(value = "endpoint")
    private String endpoint;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "DEvent{" +
                "timestamp=" + timestamp +
                ", host='" + host + '\'' +
                ", ident='" + ident + '\'' +
                ", pid='" + pid + '\'' +
                ", tag='" + tag + '\'' +
                ", message='" + message + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", file='" + logfile + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
