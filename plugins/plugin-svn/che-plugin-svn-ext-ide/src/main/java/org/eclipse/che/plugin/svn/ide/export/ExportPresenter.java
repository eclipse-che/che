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
package org.eclipse.che.plugin.svn.ide.export;

import com.google.gwt.user.client.Window;
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
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.GetRevisionsResponse;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link ExportView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class ExportPresenter extends SubversionActionPresenter implements ExportView.ActionDelegate {

    private final AppContext                               appContext;
    private final ExportView                               view;
    private final SubversionClientService                  service;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    public ExportPresenter(AppContext appContext,
                           SubversionOutputConsoleFactory consoleFactory,
                           SubversionCredentialsDialog credentialsDialog,
                           ProcessesPanelPresenter processesPanelPresenter,
                           ExportView view,
                           SubversionClientService service,
                           NotificationManager notificationManager,
                           SubversionExtensionLocalizationConstants constants,
                           StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, credentialsDialog);
        this.appContext = appContext;
        this.view = view;
        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view.setDelegate(this);


    }

    public void showExport() {
        view.onShow();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void onExportClicked() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        final String givenRevision = view.isRevisionSpecified() ? view.getRevision() : null;

        final StatusNotification notification = new StatusNotification(constants.exportStarted(resources[0].getLocation().toString()), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        view.onClose();

        if (!isNullOrEmpty(givenRevision)) {
            service.getRevisions(project.getLocation(), toRelative(project, resources[0]), "1:HEAD")
                   .then(new Operation<GetRevisionsResponse>() {
                        @Override
                        public void apply(GetRevisionsResponse response) throws OperationException {
                            final List<String> pathRevisions = response.getRevisions();

                            if (pathRevisions.size() > 0) {
                                final String pathFirstRevision = pathRevisions.get(0);
                                final String pathLastRevision = pathRevisions.get(pathRevisions.size() - 1);

                                final int givenRevisionNb = Integer.valueOf(givenRevision);
                                final int pathFirstRevisionNb =
                                        Integer.valueOf(pathFirstRevision.substring(1));
                                final int pathLastRevisionNb = Integer.valueOf(pathLastRevision.substring(1));

                                final List<String> errOutput = response.getErrOutput();
                                if (errOutput != null && !errOutput.isEmpty()) {
                                    printErrors(errOutput, constants.commandInfo());
                                    notification.setTitle(constants.exportCommandExecutionError());
                                    notification.setStatus(FAIL);

                                } else if (givenRevisionNb < pathFirstRevisionNb ||
                                           givenRevisionNb > pathLastRevisionNb) {
                                    notification.setTitle(constants.exportRevisionDoNotExistForPath(givenRevision, toRelative(project, resources[0]).toString()));
                                    notification.setStatus(FAIL);

                                } else {
                                    openExportPopup(project.getLocation(), toRelative(project, resources[0]), givenRevision, notification);
                                }
                            } else {
                                notification.setTitle(constants.exportNoRevisionForPath(toRelative(project, resources[0]).toString()));
                                notification.setStatus(FAIL);
                            }
                        }
                    })
            .catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                    notification.setTitle(constants.exportCommandExecutionError() + "\n" + error.getMessage());
                    notification.setStatus(FAIL);
                }
            });
        } else {
            openExportPopup(project.getLocation(), toRelative(project, resources[0]), null, notification);
        }
    }

    private void openExportPopup(Path project, Path exportPath, String revision, StatusNotification notification) {
        final StringBuilder url = new StringBuilder(appContext.getDevMachine().getWsAgentBaseUrl() + "/svn/"
                                                    + appContext.getDevMachine().getId() + "/export" + project.toString());
        char separator = '?';
        if (!".".equals(exportPath.toString())) {
            url.append(separator).append("path").append('=').append(exportPath);
            separator = '&';
        }

        if (!isNullOrEmpty(revision)) {
            url.append(separator).append("revision").append('=').append(revision);
        }

        Window.open(url.toString(), "_self", "");
        notification.setTitle(constants.exportSuccessful(exportPath.toString()));
        notification.setStatus(SUCCESS);
    }

}
