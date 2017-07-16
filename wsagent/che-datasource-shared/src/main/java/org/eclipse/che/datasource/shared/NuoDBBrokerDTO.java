package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface NuoDBBrokerDTO {

    String getHostName();

    NuoDBBrokerDTO withHostName(String hostname);

    void setHostName(String hostname);

    int getPort();

    NuoDBBrokerDTO withPort(int port);

    void setPort(int port);
}
