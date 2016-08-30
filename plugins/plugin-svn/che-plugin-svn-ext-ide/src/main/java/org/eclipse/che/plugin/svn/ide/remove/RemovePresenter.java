/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.remove;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.RemoveAction} action.
 */
@Singleton
public class RemovePresenter extends SubversionActionPresenter {

    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    protected RemovePresenter(AppContext appContext,
                              NotificationManager notificationManager,
                              SubversionOutputConsoleFactory consoleFactory,
                              SubversionExtensionLocalizationConstants constants,
                              SubversionClientService service,
                              ProcessesPanelPresenter processesPanelPresenter,
                              StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);

        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
    }

    public void showRemove() {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        final StatusNotification notification = new StatusNotification(constants.removeStarted(resources.length), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        service.remove(project.getLocation(), toRelative(project, resources)).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandRemove());

                notification.setTitle(constants.removeSuccessful());
                notification.setStatus(SUCCESS);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notification.setTitle(constants.removeFailed());
                notification.setStatus(FAIL);
            }
        });
    }

}
