package org.eclipse.che.datasource.shared;

/**
 * Created by test on 7/15/17.
 */
public enum DatabaseType {

    POSTGRES("postgres", 5432, "postgres", ""),

    MYSQL("mysql", 3306, "root", ""),

    ORACLE("oracle", 1521, "SYSTEM", ""),

    JTDS("sqlserver", 1433, "sa", "Password123"),

    NUODB("nuodb", 48004, "dba", "goalie"),

    GOOGLECLOUDSQL("googleCloudSql", 3306, "root", "");

    private String connectorId;
    private int defaultPort;
    private String defaultUsername;
    private String defaultPassword;

    private DatabaseType(String connectorId, int defaultPort, String defaultUsername, String defaultPassword) {
        this.connectorId = connectorId;
        this.defaultPort = defaultPort;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    public String getConnectorId() {
        return connectorId;
    }


    public int getDefaultPort() {
        return defaultPort;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

}