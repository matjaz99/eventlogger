package si.matjazcerkvenik.eventlogger.db;

public class ClientConfig {

    private String connectionString;
    private String schema;
    private String username;
    private String password;
    private String hostname;
    private int port;
    private int connectionTimeout;
    private int readTimeout;

    public ClientConfig(String connectionString) {
        this.connectionString = connectionString;

        String[] a1 = connectionString.split("://");
        schema = a1[0];
        if (a1[1].contains("@")) {
            String[] a2 = a1[1].split("@");
            if (a2[0].contains(":")) {
                String[] a3 = a2[0].split(":");
                username = a3[0];
                password = a3[1];
            }
            if (a2[1].contains(":")) {
                String[] a4 = a2[1].split(":");
                hostname = a4[0];
                port = Integer.parseInt(a4[1]);
            }
        } else {
            if (a1[1].contains(":")) {
                String[] a4 = a1[1].split(":");
                hostname = a4[0];
                port = Integer.parseInt(a4[1]);
            } else {
                hostname = a1[1];
            }
        }
    }

    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Return connection string where password is masked with ********
     * @return
     */
    public String getConnectionStringMasked() {
        String s = connectionString;
        s = s.replace(username + ":" + password + "@",
                username + ":" + "•••••••••" + "@");
        return s;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
