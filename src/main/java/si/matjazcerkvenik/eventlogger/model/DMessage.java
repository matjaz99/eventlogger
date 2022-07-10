package si.matjazcerkvenik.eventlogger.model;

public class DMessage {

    private String host;
    private String ident;
    private String pid;
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DMessage{" +
                "host='" + host + '\'' +
                ", ident='" + ident + '\'' +
                ", pid='" + pid + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
