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
package org.eclipse.che.datasource.ide;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardMessages;
import org.eclipse.che.datasource.ide.newDatasource.connector.AbstractNewDatasourceConnectorPage;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnector;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnectorAgent;
import org.eclipse.che.datasource.ide.newDatasource.connector.mysql.MysqlDatasourceConnectorPage;
import org.eclipse.che.datasource.shared.DatabaseType;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.List;

public class ConnectorsInitializer {

    private final List<NewDatasourceConnector> connectors = new ArrayList<>();
    private final NewDatasourceConnectorAgent connectorAgent;

    @Inject
    public ConnectorsInitializer(final NewDatasourceConnectorAgent connectorAgent,
                                 final DatasourceUiResources resources,
                                 final NewDatasourceWizardMessages dsMessages,
                                 final Provider<MysqlDatasourceConnectorPage> mysqlConnectorPageProvider
                                ) {

        this.connectorAgent = connectorAgent;

        // counter to add different priorities to all connectors - to increment after each #register(NewDatasourceConnector)
        int connectorCounter = 0;

        // Add a new mysql connector
        List<Provider< ? extends AbstractNewDatasourceConnectorPage>> mysqlWizardPages = new ArrayList<>();
        mysqlWizardPages.add(mysqlConnectorPageProvider);
        NewDatasourceConnector connectorMysql = new NewDatasourceConnector(DatabaseType.MYSQL.getConnectorId(),
                                                                           connectorCounter,
                                                                           dsMessages.mysql(),
                                                                           resources.getMySqlLogo(),
                                                                           "com.mysql.cj.jdbc.Driver",
                                                                           mysqlWizardPages, DatabaseCategoryType.NOTCLOUD);
        this.connectors.add(connectorMysql);

        connectorCounter++;


    }

    public void initConnectors() {
        for (NewDatasourceConnector connector : this.connectors) {
            this.connectorAgent.register(connector);
        }
    }
}
