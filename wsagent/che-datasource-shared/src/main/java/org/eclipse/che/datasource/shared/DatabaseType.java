/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.shared;


public enum DatabaseType {

    MYSQL("mysql", 3306, "root", "");

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