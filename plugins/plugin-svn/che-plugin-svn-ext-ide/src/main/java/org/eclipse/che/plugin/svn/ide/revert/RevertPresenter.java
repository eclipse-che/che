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
package org.eclipse.che.plugin.svn.ide.revert;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsolePresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

public class RevertPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final SubversionClientService                  subversionClientService;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;
    private final DialogFactory                            dialogFactory;

    @Inject
    protected RevertPresenter(final AppContext appContext,
                              final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              final EventBus eventBus,
                              final WorkspaceAgent workspaceAgent,
                              final SubversionOutputConsolePresenter console,
                              final SubversionClientService subversionClientService,
                              final SubversionExtensionLocalizationConstants constants,
                              final NotificationManager notificationManager,
                              final DialogFactory dialogFactory,
                              final ProjectExplorerPresenter projectExplorerPart) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.subversionClientService = subversionClientService;
        this.constants = constants;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
    }

    public void show() {
        CurrentProject project = getActiveProject();
        if (project == null) {
            return;
        }

        List<String> paths = getSelectedPaths();
        ConfirmDialog confirmDialog = createConfirmDialog(project, paths);
        confirmDialog.show();
    }

    private ConfirmDialog createConfirmDialog(final CurrentProject project, final List<String> paths) {
        final ConfirmCallback okCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                final StatusNotification notification = new StatusNotification(constants.revertStarted(), PROGRESS, true);
                notificationManager.notify(notification);

                subversionClientService.revert(project.getRootProject().getPath(),
                                               paths,
                                               "infinity",
                                               new AsyncRequestCallback<CLIOutputResponse>(
                                                       dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {

                                                   @Override
                                                   protected void onSuccess(CLIOutputResponse result) {

                                                       printCommand(result.getCommand());

                                                       print(result.getOutput());

                                                       List<String> errOutput = result.getErrOutput();
                                                       printAndSpace(errOutput);

                                                       if (errOutput == null || errOutput.size() == 0) {                                                           
                                                           notification.setTitle(constants.revertSuccessful());
                                                           notification.setStatus(SUCCESS);
                                                       } else {
                                                           notification.setTitle(constants.revertWarning());
                                                           notification.setStatus(SUCCESS);
                                                       }
                                                   }

                                                   @Override
                                                   protected void onFailure(Throwable exception) {
                                                       String errorMessage = exception.getMessage();
                                                       notification.setTitle(constants.revertFailed() + ": " + errorMessage);
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
        for (String path : paths) {
            if (pathsString == null) {
                pathsString = path;
            }
            else {
                pathsString += ", " + path;
            }
        }

        String confirmText = paths.size() > 0 ? constants.revertConfirmText(" to " + pathsString) : constants.revertConfirmText("");
        return dialogFactory.createConfirmDialog(constants.revertTitle(), confirmText, okCallback, cancelCallback);
    }
}
