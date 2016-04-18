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
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.GetRevisionsResponse;

import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
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

    private final AppContext appContext;
    private final ExportView view;
    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final SubversionClientService                  subversionClientService;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    private HasStorablePath selectedNode;

    @Inject
    public ExportPresenter(AppContext appContext,
                           SubversionOutputConsoleFactory consoleFactory,
                           ConsolesPanelPresenter consolesPanelPresenter,
                           ProjectExplorerPresenter projectExplorerPart,
                           ExportView view,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           SubversionClientService subversionClientService,
                           NotificationManager notificationManager,
                           SubversionExtensionLocalizationConstants constants,
                           final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);
        this.appContext = appContext;
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.subversionClientService = subversionClientService;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view.setDelegate(this);


    }

    public void showExport(HasStorablePath selectedNode) {
        this.selectedNode = selectedNode;

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
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            notificationManager.notify(constants.exportFailedNoProjectPath(), FAIL, true);
            return;
        }

        final String nullableExportPath = emptyToNull(relPath(projectPath, selectedNode.getStorablePath()));
        final String exportPath = (nullableExportPath != null ? nullableExportPath : ".");
        final String givenRevision = view.isRevisionSpecified() ? view.getRevision() : null;

        final StatusNotification notification = new StatusNotification(constants.exportStarted(exportPath), PROGRESS, true);
        notificationManager.notify(notification);

        view.onClose();

        if (!isNullOrEmpty(givenRevision)) {
            final String path = getSelectedPaths().get(0);
            subversionClientService.getRevisions(getActiveProject().getRootProject().getPath(), path, "1:HEAD",
                                                 new AsyncRequestCallback<GetRevisionsResponse>(
                                                         dtoUnmarshallerFactory.newUnmarshaller(GetRevisionsResponse.class)) {
                                                     @Override
                                                     protected void onSuccess(GetRevisionsResponse result) {
                                                         final List<String> pathRevisions = result.getRevisions();

                                                         if (pathRevisions.size() > 0) {
                                                             final String pathFirstRevision = pathRevisions.get(0);
                                                             final String pathLastRevision = pathRevisions.get(pathRevisions.size() - 1);

                                                             final int givenRevisionNb = Integer.valueOf(givenRevision);
                                                             final int pathFirstRevisionNb =
                                                                     Integer.valueOf(pathFirstRevision.substring(1));
                                                             final int pathLastRevisionNb = Integer.valueOf(pathLastRevision.substring(1));

                                                             final List<String> errOutput = result.getErrOutput();
                                                             if (errOutput != null && !errOutput.isEmpty()) {
                                                                 printErrors(errOutput, constants.commandInfo());
                                                                 notification.setTitle(constants.exportCommandExecutionError());
                                                                 notification.setStatus(FAIL);

                                                             } else if (givenRevisionNb < pathFirstRevisionNb ||
                                                                        givenRevisionNb > pathLastRevisionNb) {
                                                                 notification.setTitle(
                                                                         constants.exportRevisionDoNotExistForPath(givenRevision, path));
                                                                 notification.setStatus(FAIL);

                                                             } else {
                                                                 openExportPopup(projectPath, exportPath, givenRevision, notification);
                                                             }
                                                         } else {
                                                             notification.setTitle(constants.exportNoRevisionForPath(exportPath));
                                                             notification.setStatus(FAIL);
                                                         }
                                                     }

                                                     @Override
                                                     protected void onFailure(Throwable exception) {
                                                         notification.setTitle(constants.exportCommandExecutionError() + "\n" +
                                                                               exception.getLocalizedMessage());
                                                         notification.setStatus(FAIL);
                                                     }
                                                 });
        } else {
            openExportPopup(projectPath, exportPath, notification);
        }
    }

    private void openExportPopup(final String projectPath, final String exportPath, final StatusNotification notification) {
        openExportPopup(projectPath, exportPath, null, notification);
    }

    private void openExportPopup(final String projectPath, final String exportPath, final String revision,
                                 final StatusNotification notification) {
        final StringBuilder url = new StringBuilder(appContext.getDevMachine().getWsAgentBaseUrl() + "/svn/"
                                                    + appContext.getWorkspaceId() + "/export" + projectPath);
        char separator = '?';
        if (!".".equals(exportPath)) {
            url.append(separator).append("path").append('=').append(exportPath);
            separator = '&';
        }

        if (!isNullOrEmpty(revision)) {
            url.append(separator).append("revision").append('=').append(revision);
        }

        Window.open(url.toString(), "_self", "");
        notification.setTitle(constants.exportSuccessful(exportPath));
        notification.setStatus(SUCCESS);
    }

    /** {@inheritDoc} */
    @Override
    public void minimize() {

    }

    /** {@inheritDoc} */
    @Override
    public void activatePart() {

    }

    private String relPath(String base, String path) {
        if (!path.startsWith(base)) {
            return null;
        }

        final String temp = path.substring(base.length());

        return temp.startsWith("/") ? temp.substring(1) : temp;
    }
}
