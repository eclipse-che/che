package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface NuoDBBrokerDTO {

    String getHostName();

    void setHostName(String hostname);

    NuoDBBrokerDTO withHostName(String hostname);

    int getPort();

    void setPort(int port);

    NuoDBBrokerDTO withPort(int port);
}
