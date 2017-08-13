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
import com.google.inject.Singleton;

import org.eclipse.che.datasource.ide.AvailableJdbcDriversService;
import org.eclipse.che.datasource.ide.newDatasource.presenter.NewDatasourceWizardPresenter;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

import javax.validation.constraints.NotNull;


@Singleton
public class NewDatasourceWizardAction extends Action {

    /** The {@link NotificationManager} used to show start, completion or error messages to the user. */
    protected NotificationManager   notificationManager;

    private final NewDatasourceWizardPresenter wizard;
    private final AvailableJdbcDriversService availableJdbcDrivers;

    @Inject
    public NewDatasourceWizardAction(@NotNull NewDatasourceWizardPresenter wizard,
                                     @NotNull final NotificationManager notificationManager,
                                     AvailableJdbcDriversService availableJdbcDrivers) {
        super("New Datasource", "Create a New Datasource");
        this.wizard = wizard;
        this.notificationManager = notificationManager;
        this.availableJdbcDrivers=availableJdbcDrivers;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    /**
     * Reaction to action activation.
     */
    public void actionPerformed() {
        try {

            availableJdbcDrivers.fetch();
            wizard.show();
        } catch (final Exception exception) {
            notificationManager.notify(exception.getLocalizedMessage(), StatusNotification.Status.FAIL,StatusNotification.DisplayMode.FLOAT_MODE );
        }
    }
}
