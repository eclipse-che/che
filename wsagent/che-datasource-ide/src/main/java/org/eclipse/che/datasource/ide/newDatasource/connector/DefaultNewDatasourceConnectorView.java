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
package org.eclipse.che.datasource.ide.newDatasource.connector;

import com.google.gwt.resources.client.ImageResource;
import com.google.inject.ImplementedBy;

/**
 * The view of datasource wizard connectors.
 */
@ImplementedBy(DefaultNewDatasourceConnectorViewImpl.class)
public interface DefaultNewDatasourceConnectorView extends AbstractNewDatasourceConnectorView<DefaultNewDatasourceConnectorView.ActionDelegate> {

    /** Required for delegating functions in view. */
    public interface ActionDelegate extends AbstractNewDatasourceConnectorView.ActionDelegate {

        void hostNameChanged(String name);

        void portChanged(int port);

        void useSSLChanged(boolean useSSL);

        void verifyServerCertificateChanged(boolean verifyServerCertificate);
    }

    String getHostname();

    int getPort();

    /**
     * Sets the port in the displayed configuration.
     *
     * @param port the new value
     */
    void setPort(int port);

    boolean getUseSSL();

    boolean getVerifyServerCertificate();

    void setDatabaseName(String databaseName);

    void setHostName(String hostName);

    void setUseSSL(boolean useSSL);

    void setVerifyServerCertificate(boolean verifyServerCertificate);

    void setUsername(String username);

    void setPassword(String password);

    void setImage(ImageResource image);

    void setDatasourceName(String dsName);
}
