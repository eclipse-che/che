package org.eclipse.che.datasource.shared.ssl;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface SslKeyStoreEntry {
    String getAlias();

    void setAlias(String alias);

    String getType();

    void setType(String type);

    SslKeyStoreEntry withAlias(String alias);

    SslKeyStoreEntry withType(String type);

}