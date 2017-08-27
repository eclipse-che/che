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
package org.eclipse.che.datasource.ide.newDatasource;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.wizard.AbstractWizard;

import javax.annotation.Nonnull;

public class NewDatasourceWizard extends AbstractWizard<DatabaseConfigurationDTO> {
    public static final String DATASOURCE_NAME_KEY = "DatasourceName";

    private final NotificationManager notificationManager;
    private final EventBus            eventBus;

    @Inject
    public NewDatasourceWizard(@Assisted DatabaseConfigurationDTO dataObject,
                               NotificationManager notificationManager,
                               EventBus eventBus) {
        super(dataObject);
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
    }

    @Override
    public void complete(@Nonnull final CompleteCallback callback) {
    }
}
