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
package org.eclipse.che.datasource.ide.newDatasource.connector.mysql;

import com.google.inject.Inject;

import org.eclipse.che.datasource.ide.DatasourceClientService;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardMessages;
import org.eclipse.che.datasource.ide.newDatasource.connector.DefaultNewDatasourceConnectorPage;
import org.eclipse.che.datasource.ide.newDatasource.connector.DefaultNewDatasourceConnectorView;
import org.eclipse.che.datasource.shared.DatabaseType;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;

/**
 * Created by Wafa on 20/01/14.
 */
public class MysqlDatasourceConnectorPage extends DefaultNewDatasourceConnectorPage {

    @Inject
    public MysqlDatasourceConnectorPage(final DefaultNewDatasourceConnectorView view,
                                        final NotificationManager notificationManager,
                                        final DtoFactory dtoFactory,
                                        final DatasourceClientService service,
                                        final NewDatasourceWizardMessages messages) {
        super(view, service, notificationManager, dtoFactory, messages, DatabaseType.MYSQL.getDefaultPort(), DatabaseType.MYSQL);
    }
}
