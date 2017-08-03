package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface DatabaseConfigurationDTO {

    String getDatasourceId();

    void setDatasourceId(String id);

    DatabaseConfigurationDTO withDatasourceId(String type);


    String getConfigurationConnectorId();

    void setConfigurationConnectorId(String connectorId);

    DatabaseConfigurationDTO withConfigurationConnectorId(String connectorId);


    String getDatabaseName();

    void setDatabaseName(String databaseName);

    DatabaseConfigurationDTO withDatabaseName(String databaseName);


    DatabaseType getDatabaseType();

    void setDatabaseType(DatabaseType type);

    DatabaseConfigurationDTO withDatabaseType(DatabaseType type);


    String getUsername();

    void setUsername(String username);

    DatabaseConfigurationDTO withUsername(String username);

    String getPassword();

    void setPassword(String password);

    DatabaseConfigurationDTO withPassword(String password);


    /* should be in child classes */
    String getHostName();

    void setHostName(String hostname);

    int getPort();

    void setPort(int port);


    List<NuoDBBrokerDTO> getBrokers();

    void setBrokers(List<NuoDBBrokerDTO> brokers);


    boolean getUseSSL();

    void setUseSSL(boolean useSSL);

    DatabaseConfigurationDTO withUseSSL(boolean useSSL);

    boolean getVerifyServerCertificate();

    void setVerifyServerCertificate(boolean verifyServerCertificate);

    DatabaseConfigurationDTO withVerifyServerCertificate(boolean verifyServerCertificate);

    Long getRunnerProcessId();

    void setRunnerProcessId(Long processId);

    DatabaseConfigurationDTO withRunnerProcessId(Long runnerProcessId);
}