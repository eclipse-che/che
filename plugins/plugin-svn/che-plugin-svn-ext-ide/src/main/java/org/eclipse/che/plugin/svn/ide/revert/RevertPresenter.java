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
package org.eclipse.che.plugin.svn.ide.revert;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

public class RevertPresenter extends SubversionActionPresenter {

    private final SubversionClientService                  service;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;
    private final DialogFactory                            dialogFactory;

    @Inject
    protected RevertPresenter(AppContext appContext,
                              SubversionOutputConsoleFactory consoleFactory,
                              ProcessesPanelPresenter processesPanelPresenter,
                              SubversionClientService service,
                              SubversionExtensionLocalizationConstants constants,
                              SubversionCredentialsDialog credentialsDialog,
                              NotificationManager notificationManager,
                              DialogFactory dialogFactory,
                              StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, credentialsDialog);
        this.service = service;
        this.constants = constants;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
    }

    public void show() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        ConfirmDialog confirmDialog = createConfirmDialog(project, resources);
        confirmDialog.show();
    }

    private ConfirmDialog createConfirmDialog(final Project project, final Resource[] resources) {
        final ConfirmCallback okCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                final StatusNotification notification = new StatusNotification(constants.revertStarted(), PROGRESS, FLOAT_MODE);
                notificationManager.notify(notification);

                service.revert(project.getLocation(), toRelative(project, resources), "infinity").then(new Operation<CLIOutputResponse>() {
                    @Override
                    public void apply(CLIOutputResponse response) throws OperationException {
                        List<String> errOutput = response.getErrOutput();
                        printResponse(response.getCommand(), response.getOutput(), errOutput, "svn revert");

                        if (errOutput == null || errOutput.size() == 0) {
                            notification.setTitle(constants.revertSuccessful());
                            notification.setStatus(SUCCESS);
                        } else {
                            notification.setTitle(constants.revertWarning());
                            notification.setStatus(SUCCESS);
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        notification.setTitle(constants.revertFailed());
                        notification.setStatus(FAIL);
                    }
                });
            }
        };

        final CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {

            }
        };

        String pathsString = null;
        for (Resource resource : resources) {
            if (pathsString == null) {
                pathsString = resource.getLocation().toString();
            }
            else {
                pathsString += ", " + resource.getLocation().toString();
            }
        }

        String confirmText = resources.length > 0 ? constants.revertConfirmText(" to " + pathsString) : constants.revertConfirmText("");
        return dialogFactory.createConfirmDialog(constants.revertTitle(), confirmText, okCallback, cancelCallback);
    }
}
