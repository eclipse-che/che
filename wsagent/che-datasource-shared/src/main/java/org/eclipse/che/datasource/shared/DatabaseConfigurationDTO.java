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

    DatabaseConfigurationDTO withUsername(String username);

    void setUsername(String username);


    String getPassword();

    void setPassword(String password);

    DatabaseConfigurationDTO withPassword(String password);


    /* should be in child classes */
    String getHostName();

    int getPort();


    void setHostName(String hostname);

    void setPort(int port);


    List<NuoDBBrokerDTO> getBrokers();

    void setBrokers(List<NuoDBBrokerDTO> brokers);


    boolean getUseSSL();

    DatabaseConfigurationDTO withUseSSL(boolean useSSL);

    void setUseSSL(boolean useSSL);


    boolean getVerifyServerCertificate();

    DatabaseConfigurationDTO withVerifyServerCertificate(boolean verifyServerCertificate);

    void setVerifyServerCertificate(boolean verifyServerCertificate);

    void setRunnerProcessId(Long processId);

    Long getRunnerProcessId();

    DatabaseConfigurationDTO withRunnerProcessId(Long runnerProcessId);
}